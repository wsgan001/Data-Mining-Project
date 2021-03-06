import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.*;
public class util {
	public static Float sdc;
	
	public ArrayList<Transaction> readDataFile(String filename) throws IOException{
		BufferedReader in;
		Pattern pattern = Pattern.compile("(\\{.+?\\})");
		in = new BufferedReader(new FileReader(filename));
		String s;
		ArrayList<Transaction> trans = new ArrayList<Transaction>();
		while((s=in.readLine())!=null){
			if (s== "")
				continue;
			Transaction transaction = new Transaction();
			
			Matcher matcher = pattern.matcher(s);
			int num=0;
	
			while(matcher.find(num)){
				ItemSet set = new ItemSet();
				String group = matcher.group();
				String record = group.substring(1, group.length()-1);
				String arr[] = record.split(",");
				for(int i=0; i < arr.length; i++){
					//System.out.printf("%s ",arr[i].replaceAll(" ", ""));
					set.items.add(Integer.valueOf(arr[i].replace(" ", "").trim()));
				}
				
				//System.out.printf("||");
				transaction.itemSets.add(set);
				num+=group.length();
			}
			//System.out.println();
			trans.add(transaction);
		}
	in.close();
	return trans;
	}
	
	public static HashMap<Integer, Float> readParaFile(String filename) throws IOException{
		BufferedReader in_para;
		Pattern pattern1 = Pattern.compile("MIS\\(([0-9]+)\\) = ([^\r\n]*)");
		Pattern pattern2 = Pattern.compile("SDC = (.*)");
		in_para = new BufferedReader(new FileReader(filename));
		String s;
		HashMap<Integer, Float> mis = new HashMap<Integer, Float>();
		while((s = in_para.readLine())!=null){
			if(s=="")
				continue;
			Matcher matcher1 = pattern1.matcher(s);
			Matcher matcher2 = pattern2.matcher(s);	
			int num = 0;
			while(matcher1.find(num)){
				String group = matcher1.group();
				//System.out.println(group);
				int indexStart = group.indexOf("(");
				int indexEnd = group.indexOf(")");
				String subGroup1 = group.substring(indexStart+1, indexEnd);
				int id = Integer.parseInt(subGroup1);
				int index = group.indexOf("=");
				String subGroup2 = group.substring(index+2);
				float numMis = Float.parseFloat(subGroup2);
				//System.out.printf("%d,%f\n",num1, num2);
				mis.put(id, numMis);
				num+=group.length();
			}
			num=0;
			while(matcher2.find(num)){
				String group = matcher2.group();
				//System.out.println(group);
				int index = group.indexOf("=");
				String subGroup = group.substring(index+2);
				float value = Float.parseFloat(subGroup);
				//System.out.printf("SCD=%f\n",value);
				sdc = value;
				num += group.length();
			}
			//System.out.println();
		}
		in_para.close();
		return mis;
	}
	public Float getSDC(){
		return this.sdc;
	}
	
	public ArrayList<Integer> sortMIS(HashMap<Integer, Float>mis){
		Object[] arr = mis.keySet().toArray();	
		ArrayList<Integer> keyset =  new ArrayList<Integer>();
		for(int i=0; i<arr.length; i++){
			keyset.add(Integer.valueOf(arr[i].toString()));
		}
		Collections.sort(keyset);
		return keyset;
	}
	
	public HashMap<Integer, Integer> contFreq(HashMap<Integer, Float>mis, ArrayList<Transaction>trans){
		HashMap<Integer, Integer>L = new HashMap<Integer, Integer>();
		for(Integer id : mis.keySet()){
			L.put(id, 0);
		}
		for(Transaction tran : trans){
			for(Integer key : mis.keySet()){
				if(this.isItemInTransaction(key, tran)){
					Integer tmp = L.get(key);
					L.put(key, tmp+1);
				}
			}
		}
		return L;
	}
	
	public Pair<Integer, Integer> isItemInElement(Integer item, Transaction tran){
		Integer pos, ind;
		Pair<Integer, Integer> pair = new Pair<Integer, Integer>(-1, -1);
		for(pos=0; pos < tran.itemSets.size(); pos++){
			ItemSet is = tran.itemSets.get(pos);
			for(ind = 0; ind < is.items.size(); ind++){
				Integer res = is.items.get(ind);
				if(res == item){
					pair.setFirst(pos);
					pair.setSecond(ind);
					return pair;
				}
			}
		}
		return pair;
	}
	
	public boolean isItemInTransaction(Integer item, Transaction tran){
		for(ItemSet is : tran.itemSets){
			if(is.items.contains(item))
				return true;
		}
		return false;
	}
	

	public HashMap<Integer, Integer> findFreqL(HashMap<Integer, Float>mis, HashMap<Integer, Integer> L, List<Integer>M, Integer n){
		//Find all frequent items.
		HashMap<Integer, Integer> freqL = new HashMap<Integer, Integer>();
		for(Integer item: M){
			Integer count = L.get(item);
			if(((float)count)/n >= mis.get(item)){
				freqL.put(item, count);
			}
		}
		return freqL;
	}
	
	public ArrayList<Transaction> removej(ArrayList<Transaction> transet, Integer i, HashMap<Integer, Integer>L, Float SDC, Integer n){
		int sind = 0;
		int flagjump = 0;
		while(sind < transet.size()){
			int eind = 0;
			while(eind < transet.get(sind).itemSets.size()){
				int iind = 0;
				while(iind < transet.get(sind).itemSets.get(eind).items.size()){
					Integer item = transet.get(sind).itemSets.get(eind).items.get(iind);
					float tmpres1 = (float)(L.get(item));
					float tmpres2 = (float)(L.get(item))/n;
					float tmpres3 = (float)(L.get(i));
					float tmpres4 = (float)(L.get(i))/n;
					float tmpres5 = Math.abs((float)(L.get(item))/n - (float)(L.get(i))/n );
					BigDecimal b = new BigDecimal(tmpres5);
					float f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					if (f1 > SDC){
						if(transet.get(sind).itemSets.size()==1 && transet.get(sind).itemSets.get(eind).items.size()==1){
							flagjump = 1;
							transet.remove(sind);
							sind -= 1;
							break;
						}
						else if(transet.get(sind).itemSets.get(eind).items.size()==1){
							transet.get(sind).itemSets.remove(eind);
							eind -= 1;
							break;
						}
						else{
							transet.get(sind).itemSets.get(eind).items.remove(iind);
							iind -= 1;
						}
					}
					iind += 1;
				}
				if(flagjump == 1){
					flagjump = 0;
					break;
				}
				eind += 1;
			}
			sind += 1;
		}
		return transet;
	}
	
	public ArrayList<Pair<Transaction,Integer>> removek(ArrayList<Pair<Transaction,Integer>> pairSet, HashMap<Integer, Integer>L, Float SDC, Integer n){
		int sind = 0;
		int flagjump = 0;
		while(sind < pairSet.size()){
			Transaction tran = pairSet.get(sind).getFirst();
			int eind = 0;
			while(eind < tran.itemSets.size()){
				int iind = 0;
				while(iind < tran.itemSets.get(eind).items.size()){
					Integer item = tran.itemSets.get(eind).items.get(iind);
					Integer item2;
					int idx1 = eind;
					int idx2;
					while(idx1 < tran.itemSets.size()){
						if(idx1 == eind)
							idx2 = iind;
						else
							idx2 = 0;
						while(idx2 < tran.itemSets.get(idx1).items.size()){
							item2 = tran.itemSets.get(idx1).items.get(idx2);
							float tmpres = Math.abs((float)(L.get(item))/n - (float)(L.get(item2))/n );
							BigDecimal b = new BigDecimal(tmpres);
							float f = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
							if (f > SDC){
								flagjump = 1;
								pairSet.remove(sind);
								sind -= 1;
								break;
							}
							idx2++;
						}
						if(flagjump == 1)
							break;
						idx1++;
						
					}
					if(flagjump == 1)
						break;
					iind += 1;
				}
				if(flagjump == 1){
					flagjump = 0;
					break;
				}
				eind += 1;
			}
			sind += 1;
		}
		return pairSet;
	}
	
	
	public Pair<Integer, Integer> isContained(Transaction query, Transaction tran){
		int lenQuery = query.itemSets.size();
		int lenTran = tran.itemSets.size();
		int i=0; 
		int j=0;
		if(lenQuery>lenTran)
			return new Pair<Integer, Integer>(-1, -1);
		while(i<lenQuery && j<lenTran){
			if(query.itemSets.get(i).items.size()>tran.itemSets.get(j).items.size()){
				//no change in tran[j]
				j += 1;
				continue;
			}else{
				ArrayList<Integer>setQuery = query.itemSets.get(i).items;
				ArrayList<Integer>setTran = tran.itemSets.get(j).items;
				if(setTran.containsAll(setQuery)){
					i++;j++;
				}
				else{
					j++;
				}
			}
		}
		if(i < lenQuery)
			return new Pair<Integer, Integer>(-1,-1);
		else{
			int pos = j-1;
			int index = tran.itemSets.get(pos).items.indexOf(query.getLastItem());
			return new Pair<Integer, Integer>(pos,index);
		}
	}
	public static ArrayList<Transaction> removei(ArrayList<Transaction> S, Integer i){
		int sind=0;
		boolean flag = false;
		while(sind < S.size()){
			int eind=0;
			while(eind < S.get(sind).itemSets.size()){
				int iind=0;
				while(iind < S.get(sind).itemSets.get(eind).items.size()){
					Integer item = S.get(sind).getItem(eind, iind);
					if(item == i){
						if(S.get(sind).itemSets.size()==1 && S.get(sind).itemSets.get(eind).items.size()==1){
							flag = true;
							S.remove(sind);
							sind--;
							break;
						}
						else if(S.get(sind).itemSets.get(eind).items.size()==1){
							Transaction tmp = S.get(sind);
							tmp.itemSets.remove(eind);
							S.set(sind, tmp);
							eind--;
							break;
						}
						else{
							S.get(sind).itemSets.get(eind).items.remove(iind);
							iind--;
						}
					}
					iind++;
				}
				if(flag){
					flag=false;
					break;
				}
				eind++;
			}
			sind++;
		}
		return S;
	}
}
