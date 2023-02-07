resource "aws_sns_topic" "unhandled_events" {
  name = "${var.environment}-${var.component_name}-unhandled-events-sns-topic"
  kms_master_key_id = aws_kms_key.unhandled_events.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-${var.component_name}-unhandled-events-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "suspensions" {
  name = "${var.environment}-${var.component_name}-suspensions-sns-topic"
  kms_master_key_id = aws_kms_key.suspensions.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-${var.component_name}-suspensions-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "dlq" {
  name = "${var.environment}-${var.component_name}-dlq-sns-topic"
  kms_master_key_id = aws_kms_key.dlq.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-${var.component_name}-dlq-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "nems_audit" {
  name = "${var.environment}-nems-event-processor-incoming-audit-topic"
  kms_master_key_id = aws_kms_key.nems_audit.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-nems-event-processor-incoming-audit-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "re_registrations_topic" {
  name = "${var.environment}-${var.component_name}-re-registrations-sns-topic"
  kms_master_key_id = aws_kms_key.re_registrations.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-${var.component_name}-re-registrations-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic_policy" "sns_cross_account_permissions_policy" {
  arn    = aws_sns_topic.re_registrations_topic.arn
  policy = data.aws_iam_policy_document.sns_cross_account_permissions_policy_doc.json
}