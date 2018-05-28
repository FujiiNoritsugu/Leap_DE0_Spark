module Sort
#(parameter DATA_COUNT = 16, DATA_WIDTH = 12)
(
    input logic clock50MHz,
    input logic sort_type,
    input logic [DATA_COUNT*DATA_WIDTH-1:0] input_data,
    output logic [DATA_COUNT*DATA_WIDTH-1:0] output_data
);
    logic [DATA_COUNT*DATA_WIDTH-1:0] merge_output;
generate

    if (DATA_COUNT <= 1)
    begin
        assign output_data = input_data;
    end
    else
    begin

        logic [((DATA_COUNT/2)*DATA_WIDTH)-1:0] first_output;
        logic [((DATA_COUNT/2)*DATA_WIDTH)-1:0] second_output;
        logic [DATA_COUNT*DATA_WIDTH-1:0] temp_output;
        
        Sort #(.DATA_COUNT(DATA_COUNT/2),.DATA_WIDTH(DATA_WIDTH)) first (
            .clock50MHz(clock50MHz),
            .sort_type(0),
            .input_data(input_data[((DATA_COUNT/2)*DATA_WIDTH)-1:0]),
            .output_data(first_output));
        Sort #(.DATA_COUNT(DATA_COUNT/2),.DATA_WIDTH(DATA_WIDTH)) second (
            .clock50MHz(clock50MHz),
            .sort_type(1),            
            .input_data(input_data[DATA_COUNT*DATA_WIDTH-1:(DATA_COUNT/2)*DATA_WIDTH]),
            .output_data(second_output));
        assign temp_output = {second_output,first_output};

        Merge #(.DATA_COUNT(DATA_COUNT),.DATA_WIDTH(DATA_WIDTH)) merge(
        .clock50MHz(clock50MHz),
        .sort_type(sort_type),
        .input_data(temp_output),
        .output_data(merge_output));
        assign output_data = merge_output;
    end
endgenerate

endmodule

module Merge
#(parameter DATA_COUNT = 16, DATA_WIDTH = 12)
(
    input logic clock50MHz,
    input logic sort_type,
    input logic [DATA_COUNT*DATA_WIDTH-1:0] input_data,
    output logic [DATA_COUNT*DATA_WIDTH-1:0] output_data
);

generate

    if (DATA_COUNT == 1)
    begin
        assign output_data = input_data;
    end
    else
    begin
        logic [DATA_COUNT*DATA_WIDTH-1:0] compare_output;
        Compare #(.DATA_COUNT(DATA_COUNT),.DATA_WIDTH(DATA_WIDTH)) compare (
        .sort_type(sort_type),
        .input_data(input_data),
        .output_data(compare_output));

        logic [((DATA_COUNT/2)*DATA_WIDTH)-1:0] first_output;
        logic [((DATA_COUNT/2)*DATA_WIDTH)-1:0] second_output;

        Merge #(.DATA_COUNT(DATA_COUNT/2),.DATA_WIDTH(DATA_WIDTH)) first (
            .clock50MHz(clock50MHz),
            .sort_type(sort_type),
            .input_data(compare_output[((DATA_COUNT/2)*DATA_WIDTH)-1:0]),
            .output_data(first_output));
        Merge #(.DATA_COUNT(DATA_COUNT/2),.DATA_WIDTH(DATA_WIDTH)) second (
            .clock50MHz(clock50MHz),
            .sort_type(sort_type),            
            .input_data(compare_output[DATA_COUNT*DATA_WIDTH-1:(DATA_COUNT/2)*DATA_WIDTH]),
            .output_data(second_output));
        assign output_data = {second_output,first_output};
    end
endgenerate

endmodule

module Compare
#(parameter DATA_COUNT = 16, DATA_WIDTH = 12)
(
    input logic sort_type,
    input logic [DATA_COUNT*DATA_WIDTH-1:0]input_data,
    output logic [DATA_COUNT*DATA_WIDTH-1:0]output_data
);

//16データのソートを試す
logic [DATA_COUNT*DATA_WIDTH-1:0]temp_data;
parameter DIST = DATA_COUNT/2;

generate
    genvar i;
    for (i=0; i<DIST; i=i+1)
    begin: exchange_loop
        Exchange #(.DATA_WIDTH(DATA_WIDTH))exchange(
            .sort_type(sort_type),
            .input_a(input_data[(i+1)*DATA_WIDTH-1:i*DATA_WIDTH]),
            .input_b(input_data[(i+DIST+1)*DATA_WIDTH-1:(i+DIST)*DATA_WIDTH]),
            .output_a(temp_data[(i+1)*DATA_WIDTH-1:i*DATA_WIDTH]),
            .output_b(temp_data[(i+DIST+1)*DATA_WIDTH-1:(i+DIST)*DATA_WIDTH]));
    end
endgenerate

assign output_data = temp_data;

endmodule

// ソートタイプが0の場合、昇順、1の場合、降順とする
module Exchange
#(DATA_WIDTH = 12)
(
    input logic sort_type,
    input logic [DATA_WIDTH-1:0]input_a,
    input logic [DATA_WIDTH-1:0]input_b,
    output logic [DATA_WIDTH-1:0]output_a,
    output logic [DATA_WIDTH-1:0]output_b
);

always @*
begin
case(sort_type)
    0:begin
        if (input_a > input_b) begin
            output_a = input_b;
            output_b = input_a;
        end
        else begin
            output_a = input_a;
            output_b = input_b;
        end
    end
    1:begin
        if (input_a > input_b) begin
            output_a = input_a;
            output_b = input_b;
        end
        else begin
            output_a = input_b;
            output_b = input_a;
        end
    end
endcase
end
endmodule
