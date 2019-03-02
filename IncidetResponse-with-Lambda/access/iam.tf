resource "aws_iam_instance_profile" "profile" {
  name = "JenkinsSlavesAccess-Profile"
  role = "${aws_iam_role.role.name}"
}

resource "aws_iam_role" "role" {
  name = "JenkinsSlavesAccess-Role"
  path = "/"

  assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Principal": {
               "Service": [
                    "ec2.amazonaws.com",
                    "lambda.amazonaws.com"
                ]
            },
            "Effect": "Allow"
        }
    ]
}
EOF
}

resource "aws_iam_role_policy" "s3_policy" {
  name = "UploadStaticFiles"
  role = "${aws_iam_role.role.id}"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Resource": "arn:aws:s3:::movies-database-demo/*",
      "Action": [
        "s3:PutObject"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "cloudformation:*"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "lambda:*"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "sqs:ChangeMessageVisibility",
        "sqs:CreateQueue",
        "sqs:ReceiveMessage",
        "sqs:SendMessage",
        "sqs:GetQueueAttributes",
        "sqs:GetQueueUrl",
        "sqs:*"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "iam:*"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ec2:*"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": "ec2:RunInstances",
      "Resource": [
        "arn:aws:ec2:*:*:volume/*",
        "arn:aws:ec2:*:*:instance/*"
      ],
      "Condition": {
        "StringEquals": {
          "aws:RequestTag/Name": "dd-*"
        }
      }
    },
    {
      "Effect": "Allow",
      "Action": [
        "ec2:StartInstances",
        "ec2:StopInstances",
        "ec2:RebootInstances",
        "ec2:TerminateInstances",
        "ec2:CreateTags",
        "ec2:DeleteTags",
        "ec2:AttachVolume",
        "ec2:DetachVolume",
        "ec2:DeleteVolume",
        "ec2:DeleteSnapshot"
      ],
      "Resource": "*",
      "Condition": {
        "StringEquals": {
          "ec2:ResourceTag/Name": "dd-*"
        }
      }
    },
    {
      "Action": [
        "s3:CreateBucket",
        "s3:DeleteBucket",
        "s3:GetObject",
        "s3:ListAllMyBuckets",
        "s3:ListBucket",
        "s3:PutObject"
      ],
      "Effect": "Allow",
      "Resource": "arn:aws:s3:::double-digit-*"
    }
  ]
}
EOF
}
