variable "prefix" {
  description = "The prefix for the resources"
  type        = string
}

variable "location" {
  description = "The Azure Region"
  type        = string
}

variable "resource_group_name" {
  description = "The name of the resource group"
  type        = string
}

variable "node_count" {
  description = "The number of nodes in the default node pool"
  type        = number
  default     = 1
}

variable "vm_size" {
  description = "The size of the Virtual Machine"
  type        = string
  default     = "Standard_B2s" # Cheaper option for students
}

variable "vnet_subnet_id" {
  description = "The ID of the subnet"
  type        = string
}

variable "environment" {
  description = "The environment (dev, stage, prod)"
  type        = string
}

