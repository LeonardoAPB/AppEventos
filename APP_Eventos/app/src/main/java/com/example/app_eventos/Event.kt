data class Event(
    var id: String = "",
    var tipo_evento: String = "",
    var user: String = "",
    var local_evento: String = "",
    var data: String = "",
    var hora: String = "",
    var quantidade_pessoas: String = "",
    var servicos: List<String> = listOf()
) {
}
