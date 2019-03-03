//Global variables
variable "region" {
  description = "AWS region to be specified"
}

variable "role_arn" {
  description = "jenkins role to access"
}

variable "access_key_id" {
  description = "access key id"
}

variable "secret_access_key" {
  description = "secret access key"
}

variable "session_token" {
  description = "session token "
}
