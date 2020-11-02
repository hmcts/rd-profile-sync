locals {
  preview_vault_name      = join("-", [var.raw_product, "aat"])
  non_preview_vault_name  = join("-", [var.raw_product, var.env])
  key_vault_name          = var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name

  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
}

data "azurerm_key_vault" "rd_key_vault" {
  name                = local.key_vault_name
  resource_group_name = local.key_vault_name
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name          = join("-", [var.component, "POSTGRES-USER"])
  value         = module.db-profile-sync-data.user_name
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name          = join("-", [var.component, "POSTGRES-PASS"])
  value         = module.db-profile-sync-data.postgresql_password
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name          = join("-", [var.component, "POSTGRES-HOST"])
  value         = module.db-profile-sync-data.host_name
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name          = join("-", [var.component, "POSTGRES-PORT"])
  value         = "5432"
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name          = join("-", [var.component, "POSTGRES-DATABASE"])
  value         = module.db-profile-sync-data.postgresql_database
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_resource_group" "rg" {
  name          = join("-", [var.product, var.component, var.env])
  location      = var.location
  tags          = {
    "Deployment Environment"  = var.env
    "Team Name"               = var.team_name
    "lastUpdated"             = timestamp()
  }
}

module "db-profile-sync-data" {
  source          = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product         = join("-", [var.product, var.component, "postgres-db"])
  location        = var.location
  subscription    = var.subscription
  env             = var.env
  postgresql_user = "dbsyncdata"
  database_name   = "dbsyncdata"
  common_tags     = var.common_tags
}

# =================================

data "azurerm_key_vault_secret" "oauth2_redirect_uri" {
  provider      = azurerm.azure-1
  name = "OAUTH2-REDIRECT-URI"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "oauth2_auth" {	
  provider      = azurerm.azure-1
  name = "OAUTH2-AUTH"	
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"	
}

data "azurerm_key_vault_secret" "oauth2_client_secret" {
  provider      = azurerm.azure-1
  name = "OAUTH2-CLIENT-SECRET"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "s2s_url" {
  provider      = azurerm.azure-1
  name = "s2s-url"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "s2s_microservice" {
  provider      = azurerm.azure-1
  name = "s2s-microservice"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault" "s2s_key_vault" {
  provider      = azurerm.azure-1
  name = "s2s-${local.local_env}"
  resource_group_name = "rpe-service-auth-provider-${local.local_env}"
}

data "azurerm_key_vault_secret" "search_query_from" {	
  provider      = azurerm.azure-1
  name = "SEARCH-QUERY-FROM"	
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"	
}

data "azurerm_key_vault_secret" "cron_schedule" {	
  provider      = azurerm.azure-1
  name = "CRON-SCHEDULE"	
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"	
}

data "azurerm_key_vault_secret" "oauth2_client_id" {
  provider      = azurerm.azure-1
  name = "OAUTH2-CLIENT-ID"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "s2s_secret" {	
  provider      = azurerm.azure-1
  name = "microservicekey-rd-professional-api"	
  key_vault_id = "${data.azurerm_key_vault.s2s_key_vault.id}"	
}

data "azurerm_key_vault_secret" "idam_url" {	
  provider      = azurerm.azure-1
  name = "idam-url"	
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"	
}

data "azurerm_key_vault_secret" "oauth2_client_auth" {	
  provider      = azurerm.azure-1
  name = "OAUTH2-CLIENT-AUTH"	
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"	
}

data "azurerm_key_vault_secret" "USER_PROFILE_URL" {	
  provider      = azurerm.azure-1
  name = "USER-PROFILE-URL"	
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"	
}