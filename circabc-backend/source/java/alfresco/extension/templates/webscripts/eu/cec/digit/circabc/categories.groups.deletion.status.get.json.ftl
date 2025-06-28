<#-- Escape JSON strings to ensure proper JSON formatting -->
<#escape x as jsonUtils.encodeJSONString(x)>
   ${isPending?string}
</#escape>

