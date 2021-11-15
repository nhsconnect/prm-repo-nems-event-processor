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

data "aws_iam_policy_document" "sqs_nems_event_policy_doc" {
  statement {

    effect = "Allow"

    actions = [
      "sqs:SendMessage"
    ]

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    resources = [
      aws_sqs_queue.incoming_nems_events.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [data.aws_ssm_parameter.nems_events_topic_arn.value]
      variable = "aws:SourceArn"
    }
  }
}

resource "aws_sns_topic" "unhandled_events" {
  name = "${var.environment}-${var.component_name}-unhandled-events-sns-topic"
  kms_master_key_id = data.aws_ssm_parameter.sns_sqs_kms_key_id.value

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

data "aws_iam_policy_document" "unhandled_events_sns_topic_access_to_queue" {
  statement {

    effect = "Allow"

    actions = [
      "sqs:SendMessage"
    ]

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    resources = [
      aws_sqs_queue.unhandled_events.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [aws_sns_topic.unhandled_events.arn]
      variable = "aws:SourceArn"
    }
  }
}

resource "aws_sns_topic" "suspensions" {
  name = "${var.environment}-${var.component_name}-suspensions-sns-topic"
  kms_master_key_id = data.aws_ssm_parameter.sns_sqs_kms_key_id.value

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

data "aws_iam_policy_document" "suspensions_sns_topic_access_to_queue" {
  statement {
    effect = "Allow"

    actions = [
      "sqs:SendMessage"
    ]

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    resources = [
      aws_sqs_queue.suspensions_observability.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [aws_sns_topic.suspensions.arn]
      variable = "aws:SourceArn"
    }
  }
}
