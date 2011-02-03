#! /bin/bash

if [ -z "$1" ]; then
echo Use: $0 blablabla.hex /dev/ttyUSB0
exit
fi

python ./pulse_dtr.py $2
avrdude -p m168 -C /etc/avrdude.conf -c avrisp -P $2 -b 19200 -F -U flash:w:$1