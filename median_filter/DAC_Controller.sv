module DAC_Controller(
    input logic clock10MHz,
    output logic scl,
    output logic sda,
    input logic [15:0] output_data
);

typedef enum logic[2:0] { 
    STATUS_SLEEP, STATUS_START, STATUS_DATA, STATUS_END, STATUS_FINISH
} status_type;

status_type CURRENT_STATUS=STATUS_SLEEP, NEXT_STATUS=STATUS_SLEEP;

// Clocl400KHz用
logic [4:0] counter = 0;
logic clock400KHz = 0;
logic clock400KHz_sda = 0;
parameter CL_S0 = 5'b00000;
parameter CL_S8 = 5'b01000;
parameter CL_S14 = 5'b01110;
parameter CL_S22 = 5'b10110;

// SDA設定データ
parameter sda_init_data = 18'b000001100001100111;

logic scl_data;
logic sda_data;
logic sda_data2;

logic [6:0] sleep_counter = 0;
logic [2:0] start_counter = 0;
logic [5:0] sda_counter = 0;
logic [9:0] data_counter = 0;
logic [4:0] end_counter = 0;
logic [5:0] restart_counter = 0;

initial begin
    clock400KHz = 0;
    clock400KHz_sda = 0;
end

// 方針 ブロッキング入力を使用する
always_ff @(posedge clock10MHz)
begin

    // ステータスがデータ送信に変わるタイミングでカウンタを0にクリアする
    if (CURRENT_STATUS == STATUS_START && NEXT_STATUS == STATUS_DATA) begin
        counter <= CL_S8;
        data_counter <= 0;
    end else if (CURRENT_STATUS == STATUS_DATA) begin
        // 400KHz用
        if (counter >= 24)
            counter <= 0;
        else
            counter <= counter + 1;
        
        sda <= sda_data2;
        scl <= clock400KHz;
        data_counter <= data_counter + 1'b1;
    end else begin
        scl <= scl_data;
        sda <= sda_data;
    end

    // スリープカウンタ
    if (CURRENT_STATUS == STATUS_FINISH && NEXT_STATUS == STATUS_SLEEP) begin
        sleep_counter <= 0;
    end else if(CURRENT_STATUS == STATUS_SLEEP)
        sleep_counter <= sleep_counter + 1'b1;

    // 開始カウンタ
    if (CURRENT_STATUS == STATUS_SLEEP && NEXT_STATUS == STATUS_START) begin
        start_counter <= 0;
    end else if(CURRENT_STATUS == STATUS_START)
        start_counter <= start_counter + 1'b1;

    // 終了カウンタ
    if (CURRENT_STATUS == STATUS_DATA && NEXT_STATUS == STATUS_END) begin
        end_counter <= 0;
    end else if(CURRENT_STATUS == STATUS_END)
        end_counter <= end_counter + 1'b1;

    // 再起動カウンタ
    if (CURRENT_STATUS == STATUS_END && NEXT_STATUS == STATUS_FINISH) begin
        restart_counter <= 0;
    end else if(CURRENT_STATUS == STATUS_FINISH)
        restart_counter <= restart_counter + 1'b1;

    // ステータス、scl信号、sda信号はここでしか更新しない
    CURRENT_STATUS <= NEXT_STATUS;
end

always @(counter) begin
	case(counter)
        CL_S0: clock400KHz = 1;
        CL_S8:
            begin 
            clock400KHz = 0;
            clock400KHz_sda = 0;
            end
        CL_S14: clock400KHz_sda = 1;
        CL_S22: clock400KHz_sda = 0;
	endcase
end

// 方針 ノンブロッキング入力を使用する
always @*
begin
    case (CURRENT_STATUS)
        STATUS_SLEEP: begin
                // SCL, SDAに1を送信
                scl_data = 1'b1;
                sda_data = 1'b1;
                if (sleep_counter < 7'b1111110)
                    NEXT_STATUS = STATUS_SLEEP;
                else
                    NEXT_STATUS = STATUS_START;
            end
        STATUS_START: begin
                // SDAに0を送信、3カウント遅れてSCLに0を送信
                sda_data = 1'b0;
                scl_data = 1'b1;
                if (start_counter >= 3'b111) begin
                        NEXT_STATUS = STATUS_DATA; 
                end  else
                        NEXT_STATUS = STATUS_START;
            end
        STATUS_DATA: begin
                if (data_counter > 10'h390) begin
                    NEXT_STATUS = STATUS_END;
                end else
                    NEXT_STATUS = STATUS_DATA;
            end
        STATUS_END: begin
                // 1カウント分SDAにデータ送信を行った後、30カウント分SDAに0を送信その後SDAに1を送信
                // SCLに1を送信
                scl_data = 1'b1;
                if (end_counter <= 5'b00011) begin
                    sda_data = 1'b1;
                    NEXT_STATUS = STATUS_END;
                end else if (end_counter <= 5'b11110) begin
                    sda_data = 1'b0;
                    NEXT_STATUS = STATUS_END;
                end else if (end_counter >= 5'b11111)begin
                    NEXT_STATUS = STATUS_FINISH;
                end
            end
        STATUS_FINISH: begin
                scl_data = 1'b1;
                sda_data = 1'b1;
                // 時間が経過すればステータスをスリープ状態に遷移する
                if (restart_counter < 6'b111110)
                    NEXT_STATUS = STATUS_FINISH;
                else
                    NEXT_STATUS = STATUS_SLEEP;
            end
    endcase
end

always_ff @(posedge clock400KHz_sda)
begin
    if (sda_counter <= 6'b010010) begin
        sda_data2 = sda_init_data[sda_counter];
    end else if(sda_counter <= 6'b100100) begin
        case (sda_counter)
            6'b010011: sda_data2 = output_data[15]; // D15
            6'b010100: sda_data2 = output_data[14]; // D14
            6'b010101: sda_data2 = output_data[13]; // D13
            6'b010110: sda_data2 = output_data[12]; // D12
            6'b010111: sda_data2 = output_data[11]; // D11
            6'b011000: sda_data2 = output_data[10]; // D10
            6'b011001: sda_data2 = output_data[9];  // D09
            6'b011010: sda_data2 = output_data[8]; // D08
            6'b011011: sda_data2 = 1'b0; // ACK
            6'b011100: sda_data2 = output_data[7]; // D07
            6'b011101: sda_data2 = output_data[6]; // D06
            6'b011110: sda_data2 = output_data[5]; // D05
            6'b011111: sda_data2 = output_data[4]; // D04
            6'b100000: sda_data2 = output_data[3]; // D03
            6'b100001: sda_data2 = output_data[2]; // D02
            6'b100010: sda_data2 = output_data[1]; // D01
            6'b100011: sda_data2 = output_data[0]; // D00
            6'b100100: sda_data2 = 1'b0; // ACK
		endcase
    end
    if (sda_counter >= 6'b100100)
        sda_counter <= 0;
    else
        sda_counter <= sda_counter + 1'b1;
        
end
endmodule
