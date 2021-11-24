resource "aws_kms_key" "suspensions" {
  description = "Custom KMS Key to enable server side encryption for SNS and SQS"
  policy      = data.aws_iam_policy_document.suspensions_kms_key_policy_doc.json

  tags = {
    Name        = "${var.environment}-sns-sqs-encryption-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "suspensions_encryption" {
  name          = "alias/suspensions-encryption-kms-key"
  target_key_id = aws_kms_key.suspensions.id
}

data "aws_iam_policy_document" "suspensions_kms_key_policy_doc" {
  statement {
    effect = "Allow"

    principals {
      identifiers = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"]
      type        = "AWS"
    }
    actions   = ["kms:*"]
    resources = ["*"]
  }

  statement {
    effect = "Allow"

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey*"
    ]

    resources = ["*"]
  }

  statement {
    effect = "Allow"

    principals {
      identifiers = ["cloudwatch.amazonaws.com"]
      type        = "Service"
    }

    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey*"
    ]

    resources = ["*"]
  }
}

resource "aws_ssm_parameter" "suspensions_kms_key_id" {
  name  = "/repo/${var.environment}/output/${var.repo_name}/suspensions-kms-key-id"
  type  = "String"
  value = aws_kms_key.suspensions.id

  tags = {
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}