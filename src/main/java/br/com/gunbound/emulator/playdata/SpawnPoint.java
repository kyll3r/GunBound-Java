package br.com.gunbound.emulator.playdata;

public class SpawnPoint {
    // Os nomes das variáveis devem ser EXATAMENTE iguais aos do JSON
    private Integer slot_index;
    private Integer x_min;
    private Integer x_max;
    private Integer y; // Usar Integer permite que o valor seja null

    // Getters fiz mudanças pra nao deixar o valor ir pra null mas nao sei se ta rolando nao
    public int getSlotIndex() { return (this.slot_index == null) ? 0 : this.slot_index; }
    public int getXMin() { return (this.x_min == null) ? 0 : this.x_min; }
    public int getXMax() { return (this.x_max == null) ? 0 : this.x_max; }
    public Integer getY() { return (this.y == null) ? 0 : this.y; }
}