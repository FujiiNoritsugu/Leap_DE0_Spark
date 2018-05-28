module ADC_Reader
(convst,
sck,
sdi,
sdo,
clk,
rst,
input_data,
sort_input_data,
sort_store_finish
);

output convst;
output sck;
output sdi;
input sdo;
input clk;
input rst;
output [11:0] input_data;
// 16データ保持用と16データ格納完了フラグ
output [16*12-1:0] sort_input_data;
output sort_store_finish;

//convstスタートビット送出用2000ns(100Hz)計時用カウンタ
reg [23:0] t_cyc;
parameter TCYC = 24'hfa0;
wire over2000ns = (t_cyc == TCYC - 1);

always @(posedge clk or posedge rst) begin
 if(rst) 
    t_cyc <= 24'h00;
 else if(over2000ns)
    t_cyc <= 24'h00;
 else
    t_cyc <= t_cyc + 24'h1;
end

//16データ格納用カウンタ
reg [23:0] sort_cyc;
parameter SORTCYC = 24'hfa00;
wire over16data = (sort_cyc == SORTCYC - 1);
assign sort_store_finish = over16data;

always @(posedge clk or posedge rst) begin
 if(rst) 
    sort_cyc <= 24'h00;
 else if(over16data)
    sort_cyc <= 24'h00;
 else
    sort_cyc <= sort_cyc + 24'h1;
end

//convstをスタートビットが立てば1にし、かつカウンタが2になれば０にする、それ以外は１
reg conv_reg;
reg conv_counter;
assign convst = conv_reg;
reg [11:0] input_data_reg;
reg [11:0] data_reg;
assign input_data = input_data_reg;
// 16データ格納用レジスタ
reg [16*12-1:0] sort_input_reg;
assign sort_input_data = sort_input_reg;

always  @(posedge clk or posedge rst) begin
    if(rst) begin
		  conv_reg <= 1'b0;
		  conv_counter <= 1'b0;
	 end
	 else if(over2000ns) begin
        conv_reg <= 1'b1;
        conv_counter <= 1'b1;
		// 2000ns(100Hz)毎に結果データを入力データに入れる
		input_data_reg <= data_reg;
		// 16データを格納する
		sort_input_reg <= {sort_input_reg[15*12-1:0], data_reg};
    end
    else if(conv_counter) begin
        conv_reg <= 1'b1;
        conv_counter <= 1'b0;
    end
    else
    conv_reg <= 1'b0;
end

// convstの立下りの検出
// sckの立下りの検出
reg [2:0] sreg;
wire clkfall;
always @(posedge clk or posedge rst) begin
    if(rst) begin
		sreg <= 3'b000;
	 end
    else begin
      sreg <= {sreg[1:0],conv_reg};
	 end
end

assign clkfall = sreg[2] & ~sreg[1];

// sckの出力
reg sck_clk;
reg [4:0] sck_clk_counter;
assign sck = sck_clk;
reg sdi_reg;
assign sdi = sdi_reg;
reg [6:0] sck_width_counter;
// sckの立下りごとにsdoをレジスタにいれる

always @(posedge clk or posedge rst) begin
    if(rst) begin
		sck_clk <= 1'b0;
		sck_clk_counter <= 5'b0;
		sdi_reg <= 1'b0;
		sck_width_counter <= 7'b0;
		data_reg <= 12'h0;
	end
	else if(clkfall) begin
        sck_clk <= 1'b1;
        sck_clk_counter <= 5'b1;
		sck_width_counter <= 7'b1;
    end
    else if(sck_clk_counter < 5'b11010) begin
		  if(sck_width_counter == 7'h50) begin
            sck_clk <= ~sck_clk;
			//sck_clkの立下りにsdoの値をシフトレジスタに入れる
			if(sck_clk == 1'b0)
	            data_reg <= {data_reg[10:0], sdo};
			sck_width_counter <= 7'h0;
            sck_clk_counter <= sck_clk_counter + 1'b1;
			if(sck_clk_counter == 5'b10 || sck_clk_counter == 5'b1010)
				sdi_reg <= 1'b1;
			else
				sdi_reg <= 1'b0;
		  end
		  else
		     sck_width_counter <= sck_width_counter + 1'b1;
    end
    else begin
        sck_clk <= 1'b0;
		sdi_reg <= 1'b0;
		// このタイミングでデータレジスタをクリアすると上手くデータが上がってこない
		// data_reg <= 12'b000000000000;
	end
end

endmodule
