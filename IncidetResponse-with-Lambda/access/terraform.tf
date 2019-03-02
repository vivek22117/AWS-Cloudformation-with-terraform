terraform {
  backend "s3" {
    encrypt = true
    bucket  = "double-digit-cft-devl"
    key     = "lambda/terraform.tfstate"
    region  = "us-east-1"
  }
}

provider "aws" {
  assume_role {
    role_arn     = "${var.role_arn}"
  }

  region = "${var.region}"
}
