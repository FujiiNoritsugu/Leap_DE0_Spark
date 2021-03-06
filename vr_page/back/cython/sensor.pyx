import cython
cdef extern from "csensor.h":
    int *get_sensor_data()
cdef extern from "stdlib.h":
    void free(void *ptr)

def get_sensor():
    cdef:
        int *z
        int x[1000]
    z = get_sensor_data()
    if z == NULL:
        raise MemoryError("MemoryError")
    for i in range(1000):
        x[i] = z[i]
    free(z)
    return x
