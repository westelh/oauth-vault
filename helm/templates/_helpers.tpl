{{/*
Expand the name of the chart.
*/}}
{{- define "oauth-vault.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "oauth-vault.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "oauth-vault.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "oauth-vault.labels" -}}
helm.sh/chart: {{ include "oauth-vault.chart" . }}
{{ include "oauth-vault.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "oauth-vault.selectorLabels" -}}
app.kubernetes.io/name: {{ include "oauth-vault.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "oauth-vault.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "oauth-vault.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Create the name of the ConfigMap for Vault Agent
*/}}
{{- define "oauth-vault.agentConfigmapName" -}}
{{ include "oauth-vault.fullname" . }}-agent-config
{{- end }}

{{/*
Create the name of the ConfigMap for Vault Proxy
*/}}
{{- define "oauth-vault.proxyConfigmapName" -}}
{{ include "oauth-vault.fullname" . }}-proxy-config
{{- end }}

{{/*
Create the name of the ConfigMap for Vault
*/}}
{{- define "oauth-vault.vaultConfigmapName" -}}
{{ include "oauth-vault.fullname" . }}-vault-config
{{- end }}

{{/*
Create the auth config block for vault sidecars
*/}}
{{- define "oauth-vault.sidecarAuthMethod" -}}
{{- with .Values.vault.server.auth -}}
method {
  {{- with .kubernetes }}
  type = "kubernetes"
  config = {
    role = {{ .role | quote }}
  }
  {{- end }}
}
{{- end -}}
{{- end }}

{{- define "oauth-vault.applicationConfigPath" -}}
/vault/secrets/config.yaml
{{- end }}