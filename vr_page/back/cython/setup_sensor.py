from distutils.core import setup, Extension
from Cython.Build import cythonize

ext = Extension("sensor", sources=["sensor.pyx", "csensor.c"], include_dirs=['.'])
setup(name="sensor", ext_modules=cythonize([ext]))
