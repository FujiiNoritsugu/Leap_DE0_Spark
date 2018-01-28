// Copyright (C) 2017  Intel Corporation. All rights reserved.
// Your use of Intel Corporation's design tools, logic functions 
// and other software and tools, and its AMPP partner logic 
// functions, and any output files from any of the foregoing 
// (including device programming or simulation files), and any 
// associated documentation or information are expressly subject 
// to the terms and conditions of the Intel Program License 
// Subscription Agreement, the Intel Quartus Prime License Agreement,
// the Intel MegaCore Function License Agreement, or other 
// applicable license agreement, including, without limitation, 
// that your use is for the sole purpose of programming logic 
// devices manufactured by Intel and sold by Intel or its 
// authorized distributors.  Please refer to the applicable 
// agreement for further details.

// PROGRAM		"Quartus Prime"
// VERSION		"Version 17.0.0 Build 595 04/25/2017 SJ Lite Edition"
// CREATED		"Tue Nov  7 06:27:39 2017"

module Top_20171101(
	in_0,
	scl,
	sda,
	input_sw1,
	input_sw2,
	input_sw3,
	input_sw4
);


input wire	in_0;
output wire	scl;
output wire sda;
input wire input_sw1;
input wire input_sw2;
input wire input_sw3;
input wire input_sw4;

wire temp;

PLL_Test20171101	b2v_inst(
	.refclk(in_0),
	.outclk_0(temp));

Test20171220 test20171220_inst (
    .clock10MHz(temp),
    .scl(scl),
    .sda(sda),
    .input_sw1(input_sw1),
    .input_sw2(input_sw2),
    .input_sw3(input_sw3),
    .input_sw4(input_sw4)
);
endmodule
