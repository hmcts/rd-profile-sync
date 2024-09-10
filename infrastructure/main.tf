locals {
  preview_vault_name     = join("-", [var.raw_product, "aat"])
  non_preview_vault_name = join("-", [var.raw_product, var.env])
  key_vault_name         = var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name

  s2s_rg_prefix            = "rpe-service-auth-provider"
  s2s_key_vault_name       = var.env == "preview" || var.env == "spreview" ? join("-", ["s2s", "aat"]) : join("-", ["s2s", var.env])
  s2s_vault_resource_group = var.env == "preview" || var.env == "spreview" ? join("-", [local.s2s_rg_prefix, "aat"]) : join("-", [local.s2s_rg_prefix, var.env])
}

resource "azurerm_resource_group" "rg" {
  name     = join("-", [var.product, var.component, var.env])
  location = var.location
  tags = {
    "Deployment Environment" = var.env
    "Team Name"              = var.team_name
    "lastUpdated"            = timestamp()
  }
}
