locals {
  incoming_queue_name = "${var.environment}-${var.component_name}-incoming-queue"
}

resource "aws_sqs_queue" "incoming_nems_events" {
  name                       = local.incoming_queue_name
  message_retention_seconds  = 1800
  kms_master_key_id = data.aws_ssm_parameter.sns_sqs_kms_key_id.value

  tags = {
    Name = local.incoming_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic_subscription" "nems_events_to_incoming_queue" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = data.aws_ssm_parameter.nems_events_topic_arn.value
  endpoint             = aws_sqs_queue.incoming_nems_events.arn
}

resource "aws_sqs_queue_policy" "incoming_nems_events_subscription" {
  queue_url = aws_sqs_queue.incoming_nems_events.id
  policy    = data.aws_iam_policy_document.sqs_nems_event_policy_doc.json
}

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

resource "aws_sqs_queue" "unhandled_events" {
  name                       = "${var.environment}-${var.component_name}-unhandled-events-queue"
  message_retention_seconds  = 1800
  kms_master_key_id = data.aws_ssm_parameter.sns_sqs_kms_key_id.value

  tags = {
    Name = "${var.environment}-${var.component_name}-unhandled-events-queue"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic_subscription" "unhandled_events_to_queue" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.unhandled_events.arn
  endpoint             = aws_sqs_queue.unhandled_events.arn
}

resource "aws_sqs_queue_policy" "unhandled_events_subscription" {
  queue_url = aws_sqs_queue.unhandled_events.id
  policy    = data.aws_iam_policy_document.unhandled_events_sns_topic_access_to_queue.json
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

resource "aws_sqs_queue" "suspensions_observability" {
  name                       = "${var.environment}-${var.component_name}-suspensions-observability-queue"
  message_retention_seconds  = 1800
  kms_master_key_id = data.aws_ssm_parameter.sns_sqs_kms_key_id.value

  tags = {
    Name = "${var.environment}-${var.component_name}-suspensions-observability-queue"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic_subscription" "suspensions_events_to_observability_queue" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.suspensions.arn
  endpoint             = aws_sqs_queue.suspensions_observability.arn
}

resource "aws_sqs_queue_policy" "suspensions_subscription" {
  queue_url = aws_sqs_queue.suspensions_observability.id
  policy    = data.aws_iam_policy_document.suspensions_sns_topic_access_to_queue.json
}

# Dead Letter Queue
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

resource "aws_sqs_queue" "dlq" {
  name                       = "${var.environment}-${var.component_name}-dlq"
  message_retention_seconds  = 1800
  kms_master_key_id = aws_ssm_parameter.dlq_kms_key_id.value

  tags = {
    Name = "${var.environment}-${var.component_name}-dlq-queue"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic_subscription" "dlq_sns_topic_to_dlq" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.dlq.arn
  endpoint             = aws_sqs_queue.dlq.arn
}

resource "aws_sqs_queue_policy" "dlq_subscription" {
  queue_url = aws_sqs_queue.dlq.id
  policy    = data.aws_iam_policy_document.dlq_sns_topic_access_to_queue.json
}
