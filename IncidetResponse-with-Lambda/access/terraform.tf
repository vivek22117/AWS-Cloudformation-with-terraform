terraform {
  backend "s3" {
    encrypt = true
    bucket  = "double-digit-cft-devl"
    key     = "lambda/terraform.tfstate"
    region  = "us-east-1"
  }
}

data "external" "aws_assume_role" {
  program = ["python3", "terraform_aws_assume_role.py"]

  query {
    role_arn = "${var.role_arn}"
    wait     = 10
  }
}

provider "aws" {
  alias = "iamrole"

  access_key = "${data.external.aws_assume_role.result["access_key"]}"
  secret_key = "${data.external.aws_assume_role.result["secret_key"]}"
  token      = "${data.external.aws_assume_role.result["token"]}"
  region     = "${var.region}"
}
