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
