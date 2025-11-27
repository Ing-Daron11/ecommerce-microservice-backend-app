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

variable "vnet_address_space" {
  description = "The address space for the VNet"
  type        = string
  default     = "10.0.0.0/16"
}

variable "subnet_address_prefix" {
  description = "The address prefix for the subnet"
  type        = string
  default     = "10.0.1.0/24"
}

