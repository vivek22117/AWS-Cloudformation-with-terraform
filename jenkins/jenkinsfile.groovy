pipeline {
    agent any

    tools {
        org.jenkinsci.plugins.terraform.TerraformInstallation "terraform-1.0.9"
    }

    options {
        preserveStashes(buildCount: 5)
        skipStagesAfterUnstable()
    }
    parameters {
        string(name: 'VPC_NAME', defaultValue: 'vpc-subnet-network-by-vivek', description: 'Name of VPC Created')
        string(name: 'REGION', defaultValue: 'us-east-1', description: 'AWS region specified')
        string(name: 'WORKSPACE', defaultValue: 'development', description: 'worspace to use in Terraform')
    }
    environment {
        TF_HOME = tool('terraform-1.0.9')
        TF_IN_AUTOMATION = "true"
        PATH = "$TF_HOME:$PATH"
        AWS_METADATA_URL = "http://169.254.169.254:80/latest"
        AWS_METADATA_TIMEOUT = "2s"
    }

    stages {
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
                        try {
                            sh "terraform workspace new ${param.WORKSPACE}"
                        } catch (err) {
                            sh "terraform workspace select ${param.WORKSPACE}"
                        }
                        sh "terraform plan -var 'region=${param.REGION}' -out terraform-role-policy.tfplan;echo \$? > status"
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
                            sh "terraform destroy -var 'region=${param.REGION}' -force"
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