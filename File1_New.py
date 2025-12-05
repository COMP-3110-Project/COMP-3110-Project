class File1:
    def __init__(self, day: int, month: int, year: int):
        self.day = day
        self.month = month
        self.year = year
    @classmethod
    def from_file1(cls, other: "File1") -> "File1":
        """Copy constructor equivalent."""
        return cls(other.day, other.month, other.year)
    @staticmethod
    def month_to_num(m: str) -> int:
        """Convert month abbreviation to number.
        Returns 0 for unknown values."""
        if not m:
            return 0
        m_lower = m.lower()
        if m_lower in ("jan",):
            return 1
        if m_lower in ("feb",):
            return 2
        if m_lower in ("mar",):
            return 3
        if m_lower in ("apr",):
            return 4
        if m_lower in ("may",):
            return 5
        if m_lower in ("jun",):
            return 6
        if m_lower in ("jul",):
            return 7
        if m_lower in ("aug",):
            return 8
        if m_lower in ("sep",):
            return 9
        if m_lower in ("oct",):
            return 10
        if m_lower in ("nov",):
            return 11
        if m_lower in ("dec",):
            return 12
        return 0
