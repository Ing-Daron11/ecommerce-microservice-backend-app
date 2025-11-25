resource "azurerm_container_registry" "acr" {
  name                = "${replace(var.prefix, "-", "")}acr" # Must be alphanumeric
  resource_group_name = var.resource_group_name
  location            = var.location
  sku                 = "Basic" # Cheapest option
  admin_enabled       = true
}

# Grant AKS access to pull images from ACR
resource "azurerm_role_assignment" "aks_acr_pull" {
  principal_id                     = var.aks_principal_id
  role_definition_name             = "AcrPull"
  scope                            = azurerm_container_registry.acr.id
  skip_service_principal_aad_check = true
}
