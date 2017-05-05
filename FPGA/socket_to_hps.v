module socket_to_hps(
    input clk,
    input reset,
	 
	 output [7:0] range1,
    output [7:0] range2,

    input write,
    input [31:0] writedata,
	 input irq_flg,
    output irq,
	 input read,
	 output [31:0] readdata,
	 output debug_irq,
	 output waitrequest
);

reg [7:0] range1_intern = 8'b10000000;
reg [7:0] range2_intern = 8'b10000000;
assign range1 = range1_intern;
assign range2 = range2_intern;

reg reg_irq;
assign irq = reg_irq;
assign debug_irq = write;

reg [31:0] reg_readdata;
assign readdata = reg_readdata;

reg reg_wait;
assign waitrequest = reg_wait;

reg [1:0] wait_cnt;

always @(posedge clk) begin

if (write) begin
   if (writedata[7:0] != 8'b00000000)
        range1_intern <= writedata[7:0];
    if (writedata[15:8] != 8'b00000000)
        range2_intern <= writedata[15:8];
end

if(reset) begin
	reg_readdata <= 32'h0;
	reg_irq <= 1'b0;
end
else begin
	reg_readdata <= writedata;
	if (irq_flg)
		reg_irq <= 1'b1;
	else
		reg_irq <= 1'b0;
end

	if(wait_cnt == 2'b11) begin
	  reg_wait <= ~reg_wait;
	  wait_cnt <= 2'b00;
	end else begin
	   wait_cnt <= wait_cnt + 1'b1;
	end

end

endmodule
