variable "prefix" {
  description = "The prefix for the resources"
  type        = string
}

variable "resource_group_name" {
  description = "The name of the resource group"
  type        = string
}

variable "location" {
  description = "The Azure Region"
  type        = string
}

variable "aks_principal_id" {
  description = "The Principal ID of the AKS cluster identity"
  type        = string
}
