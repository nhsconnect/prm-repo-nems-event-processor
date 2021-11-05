resource "aws_sqs_queue" "nems_events" {
  name                       = "${var.environment}-${var.component_name}-nems-events-queue"
  message_retention_seconds  = 1800
  kms_master_key_id = data.aws_ssm_parameter.sns_sqs_kms_key_id.value

  tags = {
    Name = "${var.environment}-${var.component_name}-nems-events-queue"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic_subscription" "nems_events_to_queue" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = data.aws_ssm_parameter.nems_events_topic_arn.value
  endpoint             = aws_sqs_queue.nems_events.arn
}

resource "aws_sqs_queue_policy" "nems_events_subscription" {
  queue_url = aws_sqs_queue.nems_events.id
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
      aws_sqs_queue.nems_events.arn
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

resource "aws_sns_topic" "deductions" {
  name = "${var.environment}-${var.component_name}-deductions-sns-topic"
  kms_master_key_id = data.aws_ssm_parameter.sns_sqs_kms_key_id.value

  tags = {
    Name = "${var.environment}-${var.component_name}-deductions-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "deductions_observability" {
  name                       = "${var.environment}-${var.component_name}-deductions-observability-queue"
  message_retention_seconds  = 1800
  kms_master_key_id = data.aws_ssm_parameter.sns_sqs_kms_key_id.value

  tags = {
    Name = "${var.environment}-${var.component_name}-deductions-observability-queue"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic_subscription" "deductions_events_to_observability_queue" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.deductions.arn
  endpoint             = aws_sqs_queue.deductions_observability.arn
}

resource "aws_sqs_queue_policy" "deductions_subscription" {
  queue_url = aws_sqs_queue.deductions_observability.id
  policy    = data.aws_iam_policy_document.deductions_sns_topic_access_to_queue.json
}

data "aws_iam_policy_document" "deductions_sns_topic_access_to_queue" {
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
      aws_sqs_queue.deductions_observability.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [aws_sns_topic.deductions.arn]
      variable = "aws:SourceArn"
    }
  }
}

