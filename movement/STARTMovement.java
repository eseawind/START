/**
 * 
 */
package movement;

import java.util.List;
import java.util.Random;

import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.SimMap;
import core.Coord;
import core.Settings;
import core.SimClock;

/**
 * @author Yang Wenjing
 *
 */
public class STARTMovement extends MapBasedMovement implements 
SwitchableMovement {
	
	/** 区分车辆状态 */
	private int status;
	
	/** 判断是否超过持续时长 */
	private int timer;
	
	/** 记录节点的速度 */
	private double speed;
	/** 记录节点的持续时长 */
	private double duration;
	/**
	 * e=-0.00217593
a=0.971101
e2 =-0.00103644
a2=0.988955

f(x)=a-exp(e*x)
g(x)=a2-exp(e2*x)
	 * @param seed
	 * @return
	 */
	/** 状态0  设置持续时长的参数 */
	private static double DURATION_A_FOR_STATUS0 = 0.971101;
	private static double DURATION_PARA_FOR_STATUS0 = 0.00217593;
	
	/** 状态1 的持续时长参数*/
	private static double DURATION_A_FOR_STATUS1 = 0.988955;
	private static double DURATION_PARA_FOR_STATUS1 = 0.00103644;
		
	private static EventAwareRegions[] event_regions=null;
	
	public static final String TRANSITION_PROB_0 = "TransProbFile0";
	public static final String TRANSITION_PROB_1 = "TransProbFile1";
	public static final String CELLS_0 = "Cell0";
	public static final String CELLS_1 = "Cell1";
	/**
	 * a=0.11798
b=0.0058637
	 */
	public static double A0 = 0.11798;
	public static double A1 = 0.0058637;

	private DijkstraPathFinder pathFinder;
	
	public DijkstraPathFinder getPathFinder()
	{
		return this.pathFinder;
	}
	/**
	 * @param settings
	 */
	public STARTMovement(Settings settings) {
		super(settings);
		// TODO Auto-generated constructor stub
		this.status = rng.nextInt(2);
		this.pathFinder = new DijkstraPathFinder(getOkMapNodeTypes());
		EventAwareRegions.map = getMap();
		initEventRegions(settings);
	}
	
	public static void initEventRegions(Settings settings)
	{
		if(event_regions!=null)return;
		System.out.println("初始化两个区域");
		
		event_regions = new EventAwareRegions[2];
		event_regions[0] = new EventAwareRegions(0,settings.getSetting(CELLS_0),
				settings.getSetting(TRANSITION_PROB_0));
		event_regions[1] = new EventAwareRegions(1,settings.getSetting(CELLS_1),
				settings.getSetting(TRANSITION_PROB_1));
	}

	/**
	 * @param mbm
	 */
	public STARTMovement(STARTMovement mbm) {
		super(mbm);
		// TODO Auto-generated constructor stub
		this.status = rng.nextInt(2);
		this.pathFinder = mbm.pathFinder;
	}
	
	private int reverseStatus(int status)
	{
		return status==1?0:1;
	}
	
	/**
	 * 在这里实现
	 * 1.找到目的节点
	 * 2.获取path
	 * 3.将path返回
	 */
	@Override
	public Path getPath() {
		this.speed = generateSpeed(this.status);
		Path p = new Path(speed);
		
		this.setTimer();
		Cell c = event_regions[this.status].fromMN2Cell(this.lastMapNode);
		MapNode to = event_regions[reverseStatus(this.status)].findMapNodeInDis(this.lastMapNode.getLocation(),
				c.region_id);
		List<MapNode> nodePath = getPathFinder().getShortestPath(lastMapNode, to);
		
		// this assertion should never fire if the map is checked in read phase
		assert nodePath.size() > 0 : "No path from " + lastMapNode + " to " +
			to + ". The simulation map isn't fully connected";
				
		double dis=0;
		MapNode source = this.lastMapNode;
		for (MapNode node : nodePath) { // create a Path from the shortest path
			dis+=distance(source.getLocation(),node.getLocation());//计算实际距离
			p.addWaypoint(node.getLocation());
		}

		lastMapNode = to;
		this.status=this.status==0?1:0;//改变车辆状态。
		return p;
	}
	
	private double changeSpeed(double speed)
	{
		
		return rng.nextDouble()*44.4;
	}
	
	
	private double distance(Coord location, Coord location2) {
		// TODO Auto-generated method stub
		double x = Math.pow(location.getX()-location2.getY(), 2);
		double y = Math.pow(location.getY()-location2.getY(), 2);
		return Math.sqrt(x+y);
	}

	/**
	 * 初始化节点位置
	 * 在DTNHost中被调用
	 */
	@Override
	public Coord getInitialLocation() {
		//System.out.println("**获取初始位置**");

		MapNode node = this.event_regions[this.status].getInitMapNode();
		this.lastMapNode = node;
		return this.lastMapNode.getLocation();
	}
	
	private void setTimer() {
		this.duration = generateLastingTime(this.status);
		this.timer = SimClock.getIntTime()+(int)this.duration;
		
	}
	private double generateLastingTime(int status)
	{
		double seed = Math.random();
		if(status==0)
		{
			while(seed>cumulativeLastingTimeForStatus0(10800))
			{	
				seed = Math.random();
			}
			return generateLastingTimeForStatus0(seed);
		}
		else
		{
			while(seed>cumulativeLastingTimeForStatus1(10800))
			{	
				seed = Math.random();
			}
			return generateLastingTimeForStatus1(seed);
		}
	}
	private double generateLastingTimeForStatus1(double seed)
	{
		
		seed = rng.nextDouble()*cumulativeLastingTimeForStatus1(5000);
		
		double duration = duration1(seed);
		if(duration<=0)
		{
			System.out.println("生成duration<=0");
			duration = rng.nextDouble()*5000+1;
		}
		return duration;
		
		
		
	}
	

	private double generateLastingTimeForStatus0(double seed)
	{
		
		seed = rng.nextDouble()*cumulativeLastingTimeForStatus0(5000);
		
		double duration = duration0(seed);
		if(duration<=0)
		{
			System.out.println("生成duration<=0");
			duration = rng.nextDouble()*5000+1;
		}
		return duration;
		
		
	}

	private double cumulativeLastingTimeForStatus0(double timeLength)
	{
		return DURATION_A_FOR_STATUS0-Math.exp(-DURATION_PARA_FOR_STATUS0*timeLength);
		
	}
	private double cumulativeLastingTimeForStatus1(double timeLength)
	{
		if(timeLength<0) return 0;
		return DURATION_A_FOR_STATUS1-Math.exp(-DURATION_PARA_FOR_STATUS1*timeLength);
		
	}
	
	private double duration0(double seed)
	{
		return Math.log(DURATION_A_FOR_STATUS0-seed)/-DURATION_PARA_FOR_STATUS0;
	}
	
	private double duration1(double seed)
	{
		return Math.log(DURATION_A_FOR_STATUS1-seed)/-DURATION_PARA_FOR_STATUS1;
	}
	
	/**
	 * 生成速度
	 */
	protected double generateSpeed(double status)
	{
		// TODO get speed by the status
		if(status==0)
			return generateSpeedForStatus0();
		else
			return generateSpeedForStatus1();
			
	}

	private double generateSpeedForStatus0() {
		double seed = rng.nextDouble()*speed_dis_for_status0(44.4);
		double sp = reverse_speed_for_status0(seed);
		if(sp<0||sp>44.4)
			System.out.println(sp);
		return sp;

	}
	
	private double generateSpeedForStatus1() {
		double seed = rng.nextDouble()*speed_dis_for_status1(44.4);
		double sp = reverse_speed_for_status1(seed);
		if(sp<0||sp>44.4)
			System.out.println(sp);
		return sp;
	}
	
	private double reverse_speed_for_status0(double result)
	{
		return Math.pow(Math.log(1-result),1/1.5);
	}
	
	private double reverse_speed_for_status1(double result)
	{
		return Math.pow(Math.log(1-result),1/2.5);
	}
	
	private double speed_dis_for_status0(double x){
		return 1-1/Math.exp(A0*Math.pow(x, 1.5));
	}
	
	private double speed_dis_for_status1(double x)
	{
		return 1-1/Math.exp(A1*Math.pow(x, 2.5));
	}
	
//	private double cumulativeSpeedDistributionForStatus0(int v)
//	{
//		if(v==0)return 0.660763;
//		if(v<=40) return 0.0059774*v+0.660763;
//		if(v<=120) return 1.0-Math.exp(-0.0644895*v+0.383622);		
//		return 1.0;
//	}
//	
//	private double cumulativeSpeedDistributionForStatus1(int v)
//	{
//		if(v==0)return 0.217714;
//		if(v<=40) return 0.0127845*v+0.217714;
//		if(v<=120) return 1.0-Math.exp(-0.0642494*v+1.45314);
//		return 1.0;
//	}
	
	@Override
	public STARTMovement replicate() {
		return new STARTMovement(this);
	}

}
