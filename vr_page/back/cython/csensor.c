#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <netinet/in.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdint.h>
#include <errno.h>

#define SYSFS_FILE "/sys/bus/platform/drivers/test_int/__test_int_driver"
#define DATA_SIZE 1000

int *get_sensor_data(void){
	FILE *f;
	//static int sensor_data[DATA_SIZE] = {0};
	int *sensor_data;
	int ret;
//	int i;
	// mallocを使用してメモリデータを取得する
	// 呼び元のCythonでfreeする
	sensor_data = (int *)malloc(sizeof(int)*DATA_SIZE);

	f = fopen(SYSFS_FILE, "r");
	if(f == NULL){
printf("f==NULL\n");
		perror("fopen");
		return NULL;
	}

	ret = fread(sensor_data, sizeof(u_int), DATA_SIZE, f);
	fclose(f);

	if(ret == 0){
//printf("data ret == 0\n");
		if(errno == EAGAIN)
			printf("EAGAIN");
		return NULL;
	}

//printf("data ret = %d\n",ret);		
	/**
	for(i = 0; i < ret; i++){
		printf("sensor_data = %d\n", sensor_data[i]);
	}
	*/

	return sensor_data;
}

