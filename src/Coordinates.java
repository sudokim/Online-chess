public class Coordinates {
    int row, col;

    Coordinates() {
        this.row = 0;
        this.col = 0;
    }

    Coordinates(int row, int col) {
        this.row = row;
        this.col = col;
    }

    Coordinates(Coordinates coords) {
        if (coords == null) {
            throw new NullPointerException("coords not null");
        }

        this.row = coords.row;
        this.col = coords.col;
    }

    Coordinates add(Coordinates coords) {
        return new Coordinates(this.row + coords.row, this.col + coords.col);
    }

    Coordinates add(int row, int col) {
        return new Coordinates(this.row + row, this.col + col);
    }

    /**
     * @return Whether this coordinate is in a valid range
     */
    boolean isWithinRange() {
        return ((row >= 0) && (row < 8)) && ((col >= 0) && (col < 8));
    }

    /**
     * @return String representation of the coordinate
     */
    public String toString() {
        return String.format("%1c%1d", 'a' + col, 8 - row);
    }

    public int hashCode() {
        return this.row << 4 | this.col;
    }

    public boolean equals(Object obj) {
        assert obj instanceof Coordinates;

        return (this.row == ((Coordinates) obj).row) && (this.col == ((Coordinates) obj).col);
    }
}

