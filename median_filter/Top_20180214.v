module Top_20180214(
    clock50MHz_1,
    clock50MHz_2,
    reset,
    sdo,
    convst,
    sck,
    sdi,
    scl,
    sda
);

// クロック、リセット
input wire clock50MHz_1;
input wire clock50MHz_2;
input wire reset;

// ADC用
input wire      sdo;
output wire     convst;
output wire     sck;
output wire     sdi;

// DAC用
output wire scl;
output wire sda;

wire adc_clk;
wire dac_clk;

// データ処理用
wire [11:0] input_data;
wire [15:0] output_data;
wire temp_data[11:0];

//ソート結果保持用のデータ
wire [16*12-1:0] sort_input_data;
wire [16*12-1:0] sort_output_data;
reg [16*12-1:0] sort_input_reg;
//reg [12-1:0] target_output_data;

//20180518 ADCから出力される16個のデータを保持し、ソート後真ん中のデータをDACに渡す
Sort #(.DATA_COUNT(16), .DATA_WIDTH(12)) sort_asc(
    .clock50MHz(clock50MHz_1),
    .sort_type(0),
    .input_data(sort_input_reg),
    .output_data(sort_output_data)
);

// 16データの格納が完了した時点でソートの入力データを入れ替える
always @(posedge adc_clk)
begin
  if(sort_store_finish)
    begin
        //sort_output_data[(16/2)*12-1:(16/2-1)*12]
        sort_input_reg <= sort_input_data;
    end
end

ADC_Reader      adc_inst(
        .sdo(sdo),
        .clk(adc_clk),
        .rst(reset),
        .convst(convst),
        .sck(sck),
        .sdi(sdi),
        .input_data(input_data),
        .sort_input_data(sort_input_data),
        .sort_store_finish(sort_store_finish));

adc_50_to_40    adc40_inst1(
        .refclk(clock50MHz_1),
        .outclk_0(adc_clk));

// ソート後の真ん中のデータをDACに入力する
DAC_Controller  dac_inst(
    .clock10MHz(dac_clk),
    .scl(scl),
    .sda(sda),
    .output_data({sort_output_data[(16/2)*12-1:(16/2-1)*12],4'b0000})
);

dac_50_to_10   dac10_inst(
        .refclk(clock50MHz_2),
        .outclk_0(dac_clk));

endmodule
