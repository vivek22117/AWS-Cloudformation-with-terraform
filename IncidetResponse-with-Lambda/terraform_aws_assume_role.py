#!/usr/bin/env python3

import json
import os
import select
import sys
from time import sleep

import boto3
import botocore.exceptions


def error(message):
    print(message, file=sys.stderr)
    sys.exit(1)


def validate(data):
    """
    Query data and result data must have keys who's values are strings.
    """
    if not isinstance(data, dict):
        error('Data must be a dictionary.')
    for value in data.values():
        if not isinstance(value, str):
            error('Values must be strings.')


def assume_role():
    if not select.select([sys.stdin,], [], [], 0.0)[0]:
        error("No stdin data.")

    query = json.loads(sys.stdin.read())

    if not isinstance(query, dict):
        error("Data must be a dictionary.")

    validate(query)

    if "role_arn" not in query:
        error("Data parameter must define 'role_arn'.")

    session = boto3.Session()
    if "access_key" in query and "secret_key" in query:
        session = boto3.Session(
            aws_access_key_id=query["access_key"],
            aws_secret_access_key=query["secret_key"],
        )

    if "wait" in query:
        sleep(int(query["wait"]))

    sts = session.client("sts")
    response = {}
    try:
        response = sts.assume_role(RoleArn=query["role_arn"], RoleSessionName=os.path.basename(sys.argv[0]))
    except botocore.exceptions.ClientError as e:
        error(f"Error from AWS API: {e.response['Error']['Message']}")

    sys.stdout.write(json.dumps({
        "access_key": response["Credentials"]["AccessKeyId"],
        "secret_key": response["Credentials"]["SecretAccessKey"],
        "token": response["Credentials"]["SessionToken"],
    }))


if __name__ == '__main__':
    assume_role()