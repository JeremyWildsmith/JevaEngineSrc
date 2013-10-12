package UnitTest;

public class TriangleSearch
{
	Matrix2X2 m_worldToBarycentric;

	Vector2F[] m_vertice;

	public TriangleSearch(Vector2F a, Vector2F b, Vector2F c)
	{

		m_vertice = new Vector2F[]
		{ a, b, c };

		m_worldToBarycentric = new Matrix2X2(m_vertice[2].x - m_vertice[0].x, m_vertice[1].x - m_vertice[0].y, m_vertice[2].y - m_vertice[0].y, m_vertice[1].y - m_vertice[0].y).inverse();

	}

	public final boolean shouldInclude(Vector2F location)
	{
		Vector2F v = m_worldToBarycentric.dot(location.difference(m_vertice[0]));

		return (v.x + v.y <= 1.0F && v.x >= 0.0F && v.y >= 0.0F);
	}
}
