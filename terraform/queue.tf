locals {
  incoming_queue_name = "${var.environment}-${var.component_name}-incoming-queue"
  max_retention_period = 1209600
  thirty_minute_retention_period = 1800
}

resource "aws_sqs_queue" "incoming_nems_events" {
  name                       = local.incoming_queue_name
  message_retention_seconds  = local.thirty_minute_retention_period
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

#Unhandled Events Queue
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
  message_retention_seconds  = local.thirty_minute_retention_period
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

resource "aws_sqs_queue" "unhandled_audit" {
  name                       = "${var.environment}-${var.component_name}-unhandled-audit"
  message_retention_seconds  = local.max_retention_period
  kms_master_key_id = data.aws_ssm_parameter.sns_sqs_kms_key_id.value
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.nems_unhandled_audit_splunk_dlq.arn
    maxReceiveCount     = 4
  })

  tags = {
    Name = "${var.environment}-${var.component_name}-unhandled-audit"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "nems_unhandled_audit_splunk_dlq" {
  name                       = "${var.environment}-nems-event-processor-unhandled-audit-splunk-dlq"
  message_retention_seconds  = local.max_retention_period
  kms_master_key_id = aws_kms_key.nems_audit.id

  tags = {
    Name = "${var.environment}-unhandled-audit-dlq"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic_subscription" "unhandled_topic_to_audit_queue" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.unhandled_events.arn
  endpoint             = aws_sqs_queue.unhandled_audit.arn
}

resource "aws_sqs_queue_policy" "unhandled_audit_subscription" {
  queue_url = aws_sqs_queue.unhandled_audit.id
  policy    = data.aws_iam_policy_document.unhandled_events_sns_topic_access_to_queue.json
}

#Suspensions Queue
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
  message_retention_seconds  = local.thirty_minute_retention_period
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
  message_retention_seconds  = local.thirty_minute_retention_period
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

#Audit DLQ
resource "aws_sqs_queue" "nems_dlq_audit" {
  name                       = "${var.environment}-nems-event-processor-dlq-audit"
  message_retention_seconds  = local.max_retention_period
  kms_master_key_id = aws_ssm_parameter.dlq_kms_key_id.value
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.nems_dlq_audit_splunk_dlq.arn
    maxReceiveCount     = 4
  })

  tags = {
    Name = "${var.environment}-nems-event-processor-dlq-audit"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "nems_dlq_audit_splunk_dlq" {
  name                       = "${var.environment}-nems-event-processor-dlq-audit-splunk-dlq"
  message_retention_seconds  = local.max_retention_period
  kms_master_key_id = aws_ssm_parameter.dlq_kms_key_id.value

  tags = {
    Name = "${var.environment}-nems-event-processor-dlq-audit-splunk-dlq"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}


resource "aws_sns_topic_subscription" "dlq_sns_topic_to_nems_dlq_audit" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.dlq.arn
  endpoint             = aws_sqs_queue.nems_dlq_audit.arn
}

resource "aws_sqs_queue_policy" "nems_dlq_audit_subscription" {
  queue_url = aws_sqs_queue.nems_dlq_audit.id
  policy    = data.aws_iam_policy_document.dlq_sns_topic_access_to_queue.json
}


# Audit Queue
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

resource "aws_sqs_queue" "nems_audit" {
  name                       = "${var.environment}-nems-event-processor-incoming-audit"
  message_retention_seconds  = local.max_retention_period
  kms_master_key_id = aws_kms_key.nems_audit.id
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.nems_incoming_audit_splunk_dlq.arn
    maxReceiveCount     = 4
  })

  tags = {
    Name = "${var.environment}-nems-event-processor-incoming-audit"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "nems_incoming_audit_splunk_dlq" {
  name                       = "${var.environment}-nems-event-processor-incoming-audit-splunk-dlq"
  message_retention_seconds  = local.max_retention_period
  kms_master_key_id = aws_kms_key.nems_audit.id

  tags = {
    Name = "${var.environment}-nems-event-processor-incoming-audit-splunk-dlq"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}


resource "aws_sns_topic_subscription" "nems_audit_sns_topic_to_dlq" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.nems_audit.arn
  endpoint             = aws_sqs_queue.nems_audit.arn
}

resource "aws_sqs_queue_policy" "nems_audit_subscription" {
  queue_url = aws_sqs_queue.nems_audit.id
  policy    = data.aws_iam_policy_document.nems_audit_sns_topic_access_to_queue.json
}
