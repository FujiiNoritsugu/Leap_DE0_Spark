module Test20180913(
	clk,
	reset,
    sdo,
    convst,
    sck,
    sdi,
 memory_mem_a,         //        memory.mem_a
 memory_mem_ba,        //              .mem_ba
 memory_mem_ck,        //              .mem_ck
 memory_mem_ck_n,      //              .mem_ck_n
 memory_mem_cke,       //              .mem_cke
 memory_mem_cs_n,      //              .mem_cs_n
 memory_mem_ras_n,     //              .mem_ras_n
 memory_mem_cas_n,     //              .mem_cas_n
 memory_mem_we_n,      //              .mem_we_n
 memory_mem_reset_n,   //              .mem_reset_n
 memory_mem_dq,        //              .mem_dq
 memory_mem_dqs,       //              .mem_dqs
 memory_mem_dqs_n,     //              .mem_dqs_n
 memory_mem_odt,       //              .mem_odt
 memory_mem_dm,        //              .mem_dm
 memory_oct_rzqin     //              .oct_rzqin	
);

input wire	clk;
input wire	reset;
output wire [12:0] memory_mem_a;         //        memory.mem_a
output wire [2:0]  memory_mem_ba;        //              .mem_ba
output wire        memory_mem_ck;        //              .mem_ck
output wire        memory_mem_ck_n;      //              .mem_ck_n
output wire        memory_mem_cke;       //              .mem_cke
output wire        memory_mem_cs_n;      //              .mem_cs_n
output wire        memory_mem_ras_n;     //              .mem_ras_n
output wire        memory_mem_cas_n;     //              .mem_cas_n
output wire        memory_mem_we_n;     //              .mem_we_n
output wire        memory_mem_reset_n;   //              .mem_reset_n
inout  wire [7:0]  memory_mem_dq;        //              .mem_dq
inout  wire        memory_mem_dqs;       //              .mem_dqs
inout  wire        memory_mem_dqs_n;     //              .mem_dqs_n
output wire        memory_mem_odt;       //              .mem_odt
output wire        memory_mem_dm;        //              .mem_dm
input  wire        memory_oct_rzqin;     //              .oct_rzqin
// ADC用
input wire      sdo;
output wire     convst;
output wire     sck;
output wire     sdi;

	test_20180911 u0 (
		.clk_clk                                   (clk),                                   //                        clk.clk
		.memory_mem_a                              (memory_mem_a),                              //                     memory.mem_a
		.memory_mem_ba                             (memory_mem_ba),                             //                           .mem_ba
		.memory_mem_ck                             (memory_mem_ck),                             //                           .mem_ck
		.memory_mem_ck_n                           (memory_mem_ck_n),                           //                           .mem_ck_n
		.memory_mem_cke                            (memory_mem_cke),                            //                           .mem_cke
		.memory_mem_cs_n                           (memory_mem_cs_n),                           //                           .mem_cs_n
		.memory_mem_ras_n                          (memory_mem_ras_n),                          //                           .mem_ras_n
		.memory_mem_cas_n                          (memory_mem_cas_n),                          //                           .mem_cas_n
		.memory_mem_we_n                           (memory_mem_we_n),                           //                           .mem_we_n
		.memory_mem_reset_n                        (memory_mem_reset_n),                        //                           .mem_reset_n
		.memory_mem_dq                             (memory_mem_dq),                             //                           .mem_dq
		.memory_mem_dqs                            (memory_mem_dqs),                            //                           .mem_dqs
		.memory_mem_dqs_n                          (memory_mem_dqs_n),                          //                           .mem_dqs_n
		.memory_mem_odt                            (memory_mem_odt),                            //                           .mem_odt
		.memory_mem_dm                             (memory_mem_dm),                             //                           .mem_dm
		.memory_oct_rzqin                          (memory_oct_rzqin),
		.reset_reset_n                             (~reset),
		.sdram_socket_0_conduit_end_s_address   (sdram_address),   // sdram_socket_0_conduit_end.s_address
		.sdram_socket_0_conduit_end_s_write     (sdram_write),     //                           .s_write
		.sdram_socket_0_conduit_end_s_writedata (sdram_writedata), //                           .s_writedata
		.sdram_socket_0_conduit_end_s_finished  (sdram_finished),   //                           .s_finished
		.sdram_socket_0_conduit_end_s_waitrequest  (sdram_waitrequest),   //                           .s_waitrequest
	);

// adc用クロック
wire adc_clk;

// データ処理用
wire [11:0] input_data;
wire [15:0] output_data;

//ソート結果保持用のデータ
wire [16*12-1:0] sort_input_data;
wire [16*12-1:0] sort_output_data;
reg [16*12-1:0] sort_input_reg;

// 出力結果保持用データ
wire [12-1:0] adc_output_data;
// SDRAMへのデータ書込制御用
parameter BASE_ADDRESS = 536870912;
parameter CONST_FOR_WAIT_ALL = 24'hfa00;
parameter CONST_FOR_WAIT_HALF = 24'h7d00;
parameter CONST_FOR_ADDRESS = 1000;
// 定数
parameter DATAWIDTH = 32;
parameter ADDRESSWIDTH = 32;

// SDRAMデータ書込制御用信号
wire sdram_write;
wire [DATAWIDTH-1:0] sdram_writedata;
wire [ADDRESSWIDTH-1:0] sdram_address;
wire sdram_finished;
wire sdram_waitrequest;

reg write_reg;
reg [DATAWIDTH-1:0] writedata_reg;
reg [ADDRESSWIDTH-1:0] address_reg;  // this increments for each word
reg irq_reg;

reg [16-1:0] timing_counter_reg; // データ設定タイミングカウンタ
reg [10-1:0] address_index_reg; // アドレスインデックス

assign sdram_write = write_reg;
assign sdram_writedata = writedata_reg;
assign sdram_address = address_reg;
assign sdram_finished = irq_reg;

//20180518 ADCから出力される16個のデータを保持し、ソート後真ん中のデータをDACに渡す
/*
Sort #(.DATA_COUNT(16), .DATA_WIDTH(12)) sort_asc(
    .clock50MHz(clk),
    .sort_type(0),
    .input_data(sort_input_reg),
    .output_data(sort_output_data)
);
*/

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
        .refclk(clk),
        .outclk_0(adc_clk));

assign adc_output_data = sort_output_data[(16/2)*12-1:(16/2-1)*12];

always @(posedge adc_clk, posedge reset) begin
    if(reset) begin
        write_reg <= 1'b0;
        timing_counter_reg <= 16'b0;
        address_index_reg <= 10'b0;
    end else begin
        /*
        //--------------------------------------------
        // ソート用データが揃った時点でアドレスをインクリメントさせる
        //if (sort_store_finish) begin
            //sort_input_reg <= sort_input_data;
            // 20181005 4バイトずつずらす
            address_reg <= BASE_ADDRESS + 4 * address_index_reg;
            address_index_reg <= address_index_reg + 1'b1;
            if (address_index_reg > CONST_FOR_ADDRESS) begin
                irq_reg <= 1'b1;
                address_index_reg <= 10'b0;
            end else begin
                irq_reg <= 1'b0;
            end
        //end
        */
    //--------------------------------------------
    // ADCのソートデータが作成される時間毎にwrite信号をON→OFFさせる
        timing_counter_reg <= timing_counter_reg + 1;
        if (timing_counter_reg > CONST_FOR_WAIT_ALL) begin
            timing_counter_reg <= 16'b0;
        end else if (timing_counter_reg == CONST_FOR_WAIT_HALF) begin
            write_reg <= 1'b1;
            //---------------------------------------------------
            // 20190308 ソートを外してアドレスのインクリメントをSDRAMの書き込みと同時に入れる
            address_reg <= BASE_ADDRESS + 4 * address_index_reg;
            address_index_reg <= address_index_reg + 1'b1;
            if (address_index_reg > CONST_FOR_ADDRESS) begin
                irq_reg <= 1'b1;
                address_index_reg <= 10'b0;
            end else begin
                irq_reg <= 1'b0;
            end
            //----------------------------------------------------
        end else if (timing_counter_reg > CONST_FOR_WAIT_HALF) begin
            if (sdram_waitrequest) begin
                writedata_reg <= {20'b0, input_data};
            end
        end else begin
            write_reg <= 1'b0;
        end
    end
end

endmodule