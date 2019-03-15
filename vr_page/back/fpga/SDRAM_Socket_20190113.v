module SDRAM_Socket(
    clock50MHz,
    reset,
    master_address,
    master_byteenable,
    master_write,
    master_writedata,
	master_waitrequest,
    irq,
    sdram_address,
    sdram_write,
    sdram_writedata,
    sdram_waitrequest,
    sdram_finished
);

// クロック、リセット
input wire clock50MHz;
input wire reset;
// Avalon-MM master用信号
output wire [ADDRESSWIDTH-1:0] master_address;
output wire [BYTEENABLEWIDTH-1:0] master_byteenable;
output wire master_write;
output wire [DATAWIDTH-1:0] master_writedata;
input wire  master_waitrequest;

// データ書込完了IRQフラグ
output wire irq;

// SDRAM書き込み制御用
input wire [ADDRESSWIDTH-1:0] sdram_address;
input wire sdram_write;
input wire [DATAWIDTH-1:0] sdram_writedata;
output wire sdram_waitrequest;
input wire sdram_finished;

// 定数
parameter DATAWIDTH = 32;
parameter BYTEENABLEWIDTH = 4;
parameter ADDRESSWIDTH = 32;

// データの紐付け
assign master_byteenable = -1;  // all ones, always performing word size accesses
assign master_write = sdram_write;
assign master_writedata = sdram_writedata;
assign master_address = sdram_address;
assign sdram_waitrequest = master_waitrequest;
assign irq = sdram_finished;

endmodule
