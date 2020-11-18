import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;

public class ThreeD_Maze {

	public static void main(String[] args) {
		// Setup
		try {
			String pathname = "input.txt";
			File filename =  new File(pathname);
			InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
			BufferedReader br = new BufferedReader(reader);
			
			String algorithmType = br.readLine();
			int[] dimension = StringToInt(br.readLine().split(" "));
			String[] startPosStr = new String[3], endPosStr = new String[3];
			startPosStr = br.readLine().split(" ");
			int[] startPos = StringToInt(startPosStr);
			endPosStr = br.readLine().split(" ");
			int[] endPos = StringToInt(endPosStr);
			int avaGridsNum = Integer.parseInt(br.readLine());
			
			Map<String, ArrayList<Integer>> map = new HashMap<>();		
			//for each n in N inputs:
			for(int i = 0; i<avaGridsNum; i++) {
				String[] line = br.readLine().split(" ");
				String gridString = "";
				for(int j = 0; j<line.length; j++) {
					if(j<3) {
						String curString = line[j];
						gridString+=(curString+" ");
						if(j==2) {
							int[] position = StringToInt(gridString.split(" "));
							if(ifPosValid(position,dimension)) {
								map.put(gridString, new ArrayList<Integer>());
							}
						}
					}else {	
						map.get(gridString).add(Integer.parseInt(line[j]));
					}
				}
			}
			
			//Run algorithms
			if(!ifPosValid(startPos,dimension)||!ifPosValid(endPos,dimension)) { //if start or end is not in dimension
				writeToFileFail();
			}else if(algorithmType.equals("BFS")) {
				BFS(map,startPos,endPos);
			}else if(algorithmType.equals("UCS")) {
				UCS(map,startPos,endPos);
			}else if(algorithmType.equals("A*")) {
				Astar(map,startPos,endPos);
			}
			
			br.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	private static void BFS(Map<String, ArrayList<Integer>> map, int[] startPos, int[] endPos) {
		Map<String, String> parents = new HashMap<>();
		Queue<int[]> toCheck = new LinkedList<>();
		Set<String> checked = new HashSet<>();
		toCheck.add(startPos);
		parents.put(arrayToString(startPos),"NULL");
		while(!toCheck.isEmpty()) {
			int[] curPos = toCheck.poll();
			String curPosString = arrayToString(curPos);
			checked.add(curPosString);
			if(curPosString.equals(arrayToString(endPos))) {
				break;
			}
			if(map.containsKey(curPosString)) {
				ArrayList<Integer> nextOps = map.get(curPosString);
				for(int i = 0; i<nextOps.size();i++) {
					int[] nextPos = findNextPos(curPos,nextOps.get(i));
					if(!checked.contains(arrayToString(nextPos))) {
						toCheck.add(nextPos);
						parents.putIfAbsent(arrayToString(nextPos), curPosString);
					}
				}
			}
		}
		if(!parents.containsKey(arrayToString(endPos))) {
			writeToFileFail();
		}else {
			int pathLength = 1;
			Stack<String> stack = new Stack<>();
			if(arrayToString(endPos).equals(arrayToString(startPos))) {
				stack.push(arrayToString(endPos)+"0");
				writeToFileValid(pathLength,0,stack);
				
			}else {
				stack.add(arrayToString(endPos)+"1");
				String parent = parents.get(arrayToString(endPos));
				while(!parent.equals(arrayToString(startPos))) {
					stack.add(parent+"1");
					parent = parents.get(parent);
					pathLength++;
				}
				stack.add(parent+"0");
				pathLength++;
				writeToFileValid(pathLength,pathLength-1,stack);
			}
		}
		
		
		
	}
	
	private static void UCS(Map<String, ArrayList<Integer>> map, int[] startPos, int[] endPos) {
		PriorityQueue<String[]> toCheck = new PriorityQueue<>(new Comparator<String[]>() {
		    @Override
		    public int compare(String[] a, String[] b) {
		        return Integer.parseInt(a[1])-Integer.parseInt(b[1]);
		    }
		});
		int totalCost = 0;
		Map<String, Integer> checkedPair = new HashMap<>(); //<String of pos, totalCost>
		Map<String, String> parents = new HashMap<>();
		String[] pair = new String[2];
		pair[0] = arrayToString(startPos);
		pair[1] = String.valueOf(0);
		toCheck.add(pair);
		parents.put(arrayToString(startPos),"NULL");
		while(!toCheck.isEmpty()) {
			String[] curPosPair = toCheck.poll();
			int[] curPos = StringToInt(curPosPair[0].split(" "));
			int curCost = Integer.parseInt(curPosPair[1]);
			String curPosString = arrayToString(curPos);
			checkedPair.putIfAbsent(curPosString, curCost);
			if(curPosString.equals(arrayToString(endPos))) {
				totalCost = curCost;
				break;
			}
			if(map.containsKey(curPosString)) {
				ArrayList<Integer> nextOps = map.get(curPosString);
				for(int i = 0; i<nextOps.size();i++) {
					int[] nextPos = findNextPos(curPos,nextOps.get(i));
					if(!checkedPair.containsKey(arrayToString(nextPos))) {
						String[] nextPosPair = new String[2];
						nextPosPair[0] = arrayToString(nextPos);
						double x = curPos[0]-nextPos[0];
						double y = curPos[1]-nextPos[1];
						double z = curPos[2]-nextPos[2];		
						int singleStep = (int) Math.floor(10*Math.sqrt(x*x+y*y+z*z));
						nextPosPair[1] = String.valueOf(curCost+singleStep);
						toCheck.add(nextPosPair);
						parents.putIfAbsent(arrayToString(nextPos), curPosString);
					}
				}
			}
		}
		if(!parents.containsKey(arrayToString(endPos))) {
			writeToFileFail();
		}else {
			int pathLength = 1;
			Stack<String> stack = new Stack<>();
			if(arrayToString(endPos).equals(arrayToString(startPos))) {
				stack.push(arrayToString(endPos)+"0");
				writeToFileValid(pathLength,0,stack);
				
			}else {
				String parent = parents.get(arrayToString(endPos));
				stack.push(arrayToString(endPos)+String.valueOf(checkedPair.get(arrayToString(endPos))-checkedPair.get(parent)));
				while(!parent.equals(arrayToString(startPos))) {
					String grandParent = parents.get(parent);
					stack.push(parent+String.valueOf(checkedPair.get(parent)-checkedPair.get(grandParent)));
					parent = grandParent;
					pathLength++;
				}
				stack.push(parent+"0");
				pathLength++;
				writeToFileValid(pathLength,totalCost,stack);
			}
		}
	}
	
	private static void Astar(Map<String, ArrayList<Integer>> map, int[] startPos, int[] endPos) {
		PriorityQueue<String[]> toCheck = new PriorityQueue<>(new Comparator<String[]>() {
		    @Override
		    public int compare(String[] a, String[] b) {
		        return (Integer.parseInt(a[1])+Integer.parseInt(a[2]))-(Integer.parseInt(b[1])+Integer.parseInt(b[2]));
		    }
		});
		int totalCost = 0;
		Map<String, Integer> checkedPair = new HashMap<>(); //<String of pos, totalCost>
		Map<String, String> parents = new HashMap<>();
		String[] pair = new String[3];
		pair[0] = arrayToString(startPos);
		pair[1] = String.valueOf(0);
		pair[2] = String.valueOf(heuristic(startPos,endPos));
		toCheck.add(pair);
		parents.put(arrayToString(startPos),"NULL");
		while(!toCheck.isEmpty()) {
			String[] curPosPair = toCheck.poll();
			int[] curPos = StringToInt(curPosPair[0].split(" "));
			int curCost = Integer.parseInt(curPosPair[1]);
			String curPosString = arrayToString(curPos);
			checkedPair.putIfAbsent(curPosString, curCost);
			if(curPosString.equals(arrayToString(endPos))) {
				totalCost = curCost;
				break;
			}
			if(map.containsKey(curPosString)) {
				ArrayList<Integer> nextOps = map.get(curPosString);
				for(int i = 0; i<nextOps.size();i++) {
					int[] nextPos = findNextPos(curPos,nextOps.get(i));
					if(!checkedPair.containsKey(arrayToString(nextPos))) {
						String[] nextPosPair = new String[3];
						nextPosPair[0] = arrayToString(nextPos);
						double x = curPos[0]-nextPos[0];
						double y = curPos[1]-nextPos[1];
						double z = curPos[2]-nextPos[2];		
						int singleStep = (int) Math.floor(10*Math.sqrt(x*x+y*y+z*z));
						nextPosPair[1] = String.valueOf(curCost+singleStep);
						nextPosPair[2] = String.valueOf(heuristic(nextPos,endPos));
						toCheck.add(nextPosPair);
						parents.putIfAbsent(arrayToString(nextPos), curPosString);
					}
				}
			}
		}
		if(!parents.containsKey(arrayToString(endPos))) {
			writeToFileFail();
		}else {
			int pathLength = 1;
			Stack<String> stack = new Stack<>();
			if(arrayToString(endPos).equals(arrayToString(startPos))) {
				stack.push(arrayToString(endPos)+"0");
				writeToFileValid(pathLength,0,stack);
				
			}else {
				String parent = parents.get(arrayToString(endPos));
				stack.push(arrayToString(endPos)+String.valueOf(checkedPair.get(arrayToString(endPos))-checkedPair.get(parent)));
				while(!parent.equals(arrayToString(startPos))) {
					String grandParent = parents.get(parent);
					stack.push(parent+String.valueOf(checkedPair.get(parent)-checkedPair.get(grandParent)));
					parent = grandParent;
					pathLength++;
				}
				stack.push(parent+"0");
				pathLength++;
				writeToFileValid(pathLength,totalCost,stack);
			}
		}
	}
	
	//heuristic function
	private static int heuristic(int[] curPos, int[] endPos) {
		int x = curPos[0]-endPos[0];
		int y = curPos[1]-endPos[1];
		int z = curPos[2]-endPos[2]; 
		return (int) Math.floor(10*Math.sqrt(x*x+y*y+z*z));
	}
	
	//Check if position is in dimension
	private static boolean ifPosValid(int[] pos, int[] dimension) {
		for(int i = 0; i<3; i++) {
			if(pos[i]>=dimension[i]) {
				return false;
			}
		}
		return true;
	}
	
	//write valid res to file
	private static void writeToFileValid(int pathLength, int cost, Stack<String> stack) {
		try {
			File writename = new File("output.txt");
			writename.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			out.write(String.valueOf(cost));
			out.write("\n");
			out.write(String.valueOf(pathLength));
			out.write("\n");
			while(!stack.isEmpty()) {
				out.write(stack.pop());
				out.write("\n");
			}
			out.flush();
			out.close(); 
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//write FAIL to file
	private static void writeToFileFail() {
		try {
			File writename = new File("output.txt");
			writename.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			out.write("FAIL");
			out.flush();
			out.close(); 
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//int[] to String
	private static String arrayToString(int[] array) {
		String s = "";
		for(int i = 0; i<array.length;i++) {
			s+=(array[i]+" ");
		}
		return s;
	}
	
	//String[] to int[]
	private static int[] StringToInt(String[] strArr) {
		int[] res = new int[strArr.length];
		for(int i = 0; i<strArr.length; i++) {
			res[i] = Integer.parseInt(strArr[i]);
		}
		return res;
	}
	

	private static int[] findNextPos(int[] curPos, int operation) {
		int[] posCopy = curPos.clone();
		if(operation==1) {
			posCopy[0]++;
		}else if(operation==2) {
			posCopy[0]--;
		}else if(operation==3) {
			posCopy[1]++;
		}else if(operation==4) {
			posCopy[1]--;
		}else if(operation==5) {
			posCopy[2]++;
		}else if(operation==6) {
			posCopy[2]--;
		}else if(operation==7) {
			posCopy[0]++;
			posCopy[1]++;
		}else if(operation==8) {
			posCopy[0]++;
			posCopy[1]--;
		}else if(operation==9) {
			posCopy[0]--;
			posCopy[1]++;
		}else if(operation==10) {
			posCopy[0]--;
			posCopy[1]--;
		}else if(operation==11) {
			posCopy[0]++;
			posCopy[2]++;
		}else if(operation==12) {
			posCopy[0]++;
			posCopy[2]--;
		}else if(operation==13) {
			posCopy[0]--;
			posCopy[2]++;
		}else if(operation==14) {
			posCopy[0]--;
			posCopy[2]--;
		}else if(operation==15) {
			posCopy[1]++;
			posCopy[2]++;
		}else if(operation==16) {
			posCopy[1]++;
			posCopy[2]--;
		}else if(operation==17) {
			posCopy[1]--;
			posCopy[2]++;
		}else if(operation==18) {
			posCopy[1]--;
			posCopy[2]--;
		}
		return posCopy;
	}
}
