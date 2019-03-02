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
    role_arn     = "arn:aws:iam::ACCOUNT_ID:role/jenkins-access-roke"
    session_name = "SESSION_FOR_LAMBDA"
    external_id  = "EXTERNAL_ID_11"
  }

  region = "${var.region}"
}
