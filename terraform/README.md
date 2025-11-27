# Infrastructure Architecture

This project uses Terraform to provision the infrastructure on Azure.

## Architecture Diagram

```mermaid
graph TD
    User[User] --> LB[Azure Load Balancer]
    subgraph Azure Cloud
        subgraph Resource Group
            LB --> AKS[AKS Cluster]
            subgraph VNet
                subgraph Subnet
                    AKS
                end
            end
            AKS --> Pods[Microservices Pods]
        end
        subgraph Storage Account
            TFState[Terraform State]
        end
    end
```

## Modules

- **Network**: Provisions Virtual Network and Subnet.
- **AKS**: Provisions Azure Kubernetes Service cluster.

## Environments

- **Stage**: Staging environment for testing.
- **Prod**: Production environment.

## Setup

1. Run `setup_backend.ps1` to create the Azure Storage Account for Terraform state.
2. Navigate to `terraform/environments/stage` or `prod`.
3. Run `terraform init`.
4. Run `terraform apply`.
