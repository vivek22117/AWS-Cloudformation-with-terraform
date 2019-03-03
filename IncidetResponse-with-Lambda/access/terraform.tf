terraform {
  backend "s3" {
    encrypt = true
    bucket  = "double-digit-cft-devl"
    key     = "lambda/terraform.tfstate"
    region  = "us-east-1"
  }
}

provider "aws" {
  access_key = "${var.access_key_id}"
  secret_key = "${var.secret_access_key}"
  token      = "${var.session_token}"
  region     = "${var.region}"
}
