#!/bin/bash

echo "hello world"

sudo yum update
python --version
sudo yum install python3 -y

which python3
pip3 install --user virtualenv
pwd
cd ../../
mkdir venv
cd venv
pwd

source /home/ec2-user/.local/lib/python3.7/bin/activate
which python
pip3 install boto3

python --version
