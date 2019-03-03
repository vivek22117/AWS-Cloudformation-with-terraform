pipeline {
    agent any

    tools {
        "org.jenkinsci.plugins.terraform.TerraformInstallation" "Terraform"
    }

    options {
        preserveStashes(buildCount: 5)
        skipStagesAfterUnstable()
    }
    parameters {
        string(name: 'VPC_NAME', defaultValue: 'vpc-subnet-network-by-vivek', description: 'Name of VPC Created')
        string(name: 'REGION', defaultValue: 'us-east-1', description: 'AWS region specified')
        string(name: 'WORKSPACE', defaultValue: 'development', description: 'worspace to use in Terraform')
        string(name: 'ROLE_ARN', defaultValue: 'arn:aws:iam::979126654655:role/JenkinsSlaveRoleByTF')
        string(name: 'AWS_ACCESS_KEY_ID', defaultValue: '')
        string(name: 'SECRET_ACCESS_KEY', defaultValue: '')
        string(name: 'SESSION_TOKEN', defaultValue: '')
    }
    environment {
        TF_HOME = tool('Terraform')
        TF_IN_AUTOMATION = "true"
        PATH = "$TF_HOME:$PATH"
        AWS_METADATA_URL = "http://169.254.169.254:80/latest"
        AWS_METADATA_TIMEOUT = "2s"
    }

    stages {
        stage('update-instance') {
            steps {
                script {
                    def access_key_id = null
                    def secret_access_key = null
                    def session_token = null
                    access_key_id = sh(script: "aws sts assume-role --role-arn ${params.ROLE_ARN} --role-session-name 'dd-sts-session' \
                             --query 'Credentials.AccessKeyId' ", returnStdout: true)
                    secret_access_key = sh(script: "aws sts assume-role --role-arn ${params.ROLE_ARN} --role-session-name 'dd-sts-session' \
                             --query 'Credentials.SecretAccessKey' ", returnStdout: true)
                    session_token = sh(script: "aws sts assume-role --role-arn ${params.ROLE_ARN} --role-session-name 'dd-sts-session' \
                             --query 'Credentials.SessionToken' ", returnStdout: true)
                    $AWS_ACCESS_KEY_ID = access_key_id
                    $SECRET_ACCESS_KEY = secret_access_key
                    $SESSION_TOKEN = session_token
                }
            }
        }
        stage('role-&-policy-Init') {
            steps {
                dir('IncidetResponse-with-Lambda/access/') {
                    script {
                        sh "terraform --version"
                        sh "terraform init"
                        sh "whoami"
                    }
                }
            }
        }
        stage('role-&-policy-plan') {
            steps {
                dir('IncidetResponse-with-Lambda/access/') {
                    script {
                        echo $AWS_ACCESS_KEY_ID
                        echo $SECRET_ACCESS_KEY
                        sh "terraform plan -var 'region=${params.REGION}' -var 'role_arn=${params.ROLE_ARN}' \
                             -var 'access_key_id=$AWS_ACCESS_KEY_ID' -var 'secret_access_key=$SECRET_ACCESS_KEY' \
                             -var 'session_token=$SESSION_TOKEN' -out terraform-role-policy.tfplan; echo \$? > status"
                        def exitCode = readFile('status').trim()
                        echo "Terraform Plan Exit Code: ${exitCode}"
                        stash name: "terraform-role-policy-plan", includes: "terraform-role-policy.tfplan"
                    }
                }
            }
        }
        stage('role-&-policy-apply') {
            steps {
                dir('IncidetResponse-with-Lambda/access/') {
                    script {
                        def apply = false

                        try {
                            input message: 'confirm apply', ok: 'Apply config'
                            apply = true;
                        } catch (err) {
                            apply = false
                            sh "terraform destroy -var 'region=${params.REGION}' -force"
                            currentBuild.result = 'UNSTABLE'
                        }
                        if (apply) {
                            unstash "terraform-role-policy-plan"
                            sh "terraform apply terraform-role-policy.tfplan"
                        }
                    }
                }
            }
        }
    }
}