data "aws_caller_identity" "current" {}

data "aws_ssm_parameter" "private_zone_id" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/private-root-zone-id"
}

data "aws_ssm_parameter" "deductions_private_private_subnets" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/deductions-private-private-subnets"
}

data "aws_ssm_parameter" "deductions_private_vpc_id" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/private-vpc-id"
}

data "aws_ssm_parameter" "deductions_private_db_subnets" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/deductions-private-database-subnets"
}

data "aws_ssm_parameter" "sns_sqs_kms_key_id" {
  name = "/repo/${var.environment}/output/prm-deductions-mesh-forwarder/sns-sqs-kms-key-id"
}

data "aws_ssm_parameter" "nems_events_topic_arn" {
  name = "/repo/${var.environment}/output/prm-deductions-mesh-forwarder/nems-events-topic-arn"
}

data "aws_ssm_parameter" "suspensions_queue_arn" {
  name = "/repo/${var.environment}/output/suspension-service/suspensions-queue-arn"
}
