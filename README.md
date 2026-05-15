# Chipyard + Gemmini Setup Guide

## Clone and Build
Clone the forked Chipyard repository:
```bash
git clone https://github.com/NMJ0/chipyard.git
```

Run the Chipyard setup script (steps 6-9 skipped,which builds the set up for fpga accelerated simulation which we do not need):

```bash
cd chipyard
./build-setup.sh riscv-tools -s 6 -s 7 -s 8 -s 9
```
