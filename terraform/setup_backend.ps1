# Variables
$RESOURCE_GROUP_NAME = "tfstate-rg"
$STORAGE_ACCOUNT_NAME = "tfstate" + (Get-Random -Minimum 10000 -Maximum 99999) # Unique name
$CONTAINER_NAME = "tfstate"
$LOCATION = "eastus"

# Create Resource Group
Write-Host "Creating Resource Group $RESOURCE_GROUP_NAME..."
az group create --name $RESOURCE_GROUP_NAME --location $LOCATION

# Create Storage Account
Write-Host "Creating Storage Account $STORAGE_ACCOUNT_NAME..."
az storage account create --resource-group $RESOURCE_GROUP_NAME --name $STORAGE_ACCOUNT_NAME --sku Standard_LRS --encryption-services blob

# Create Blob Container
Write-Host "Creating Blob Container $CONTAINER_NAME..."
az storage container create --name $CONTAINER_NAME --account-name $STORAGE_ACCOUNT_NAME

Write-Host "Backend setup complete!"
Write-Host "Resource Group: $RESOURCE_GROUP_NAME"
Write-Host "Storage Account: $STORAGE_ACCOUNT_NAME"
Write-Host "Container: $CONTAINER_NAME"
Write-Host "Access Key:"
az storage account keys list --resource-group $RESOURCE_GROUP_NAME --account-name $STORAGE_ACCOUNT_NAME --query "[0].value" -o tsv
