/**
 * 
 */
package movement;

/**
 * @author yangwenjing
 *
 */
public class Cell implements Comparable<Cell>{
	public int x;
	public int y;
	public int num;
	public int region_id;
	
	public Cell(int x, int y, int num, int region_id)
	{
		this.x=x;
		this.y=y;
		this.num=num;
		this.region_id=region_id;
	}
	@Override
	public int compareTo(Cell arg0) {
		if(this.num>arg0.num)return -1;
		else if(this.num<arg0.num)return 1;
		return 0;
	}
	
	
}
