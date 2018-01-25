package test;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;
import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.auth.*;
import com.amazonaws.client.builder.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import com.waikay.aws.common.*;
import com.waikay.aws.s3.*;
import com.waikay.aws.s3.command.*;
import com.waikay.aws.s3.formatter.*;

public class S3Test {
	private AppRepository repo_ = null;
	private S3Analyzer analyzer_ = null;

	//localstack test
	private final static String S3_ENDPOINT = "http://192.168.159.129:4572";
	private final static String REGION_US_EAST = "us-east-1";

//********************************************************

	@BeforeClass
	public static void beforeClass() throws MalformedURLException {
	}

//********************************************************

	@Before
	public void setup() {
		repo_ = AppRepository.getInstance();
		analyzer_ = (S3Analyzer)repo_.getBean("s3Analyzer");
	}

//********************************************************************

	@Test
    public void s3ListBucketsTest() 
		throws Exception {
debugL("=======================================");
debugL("s3ListBucketsTest:");		
		try {
		analyzer_.init(S3_ENDPOINT, REGION_US_EAST);
		List<Bucket> buckets = analyzer_.listBuckets();
		for (Bucket bucket : buckets) {
			String bucketName = bucket.getName();
debugL("listAll: bucketName:" + bucketName + "!");
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
debugL("s3ListBucketsTest: end\n");		
	}	

//********************************************************************

	@Test
    public void s3ListAllObjectMetasTest() 
		throws Exception {
debugL("=======================================");
debugL("s3ListAllObjectMetasTest:");		
		String region = REGION_US_EAST;
		ListObjectVO listObjectVO = new ListObjectVO();
		analyzer_.init(S3_ENDPOINT, REGION_US_EAST);
		analyzer_.listAllObjectMetas(listObjectVO);
debugL("s3ListAllObjectMetasTest: end\n");		
	}	

//********************************************************************

	@Test
    public void storageKeyTest() 
		throws Exception {
debugL("=======================================");
debugL("storageKeyTest:");
		Map<StorageClassKeyVO,String> storageMap = new HashMap<StorageClassKeyVO,String>();
		StorageClassKeyVO key1 = new StorageClassKeyVO("us-east-1", "in1");
		storageMap.put(key1, "val1");
		StorageClassKeyVO key2 = new StorageClassKeyVO("us-east-1", "in2");
		storageMap.put(key2, "val2");
		StorageClassKeyVO key3 = new StorageClassKeyVO("us-east-1", "in1");
		storageMap.put(key3, "val3");
		StorageClassKeyVO key4 = new StorageClassKeyVO("us-west-1", "in1");
		storageMap.put(key4, "val4");
		StorageClassKeyVO key5 = new StorageClassKeyVO("us-east-1", "in2");
		storageMap.put(key5, "val5");
debugL("storageMap.size:" + storageMap.size() + "! storageMap:" + storageMap + "!");
		StorageClassKeyVO getkey1 = new StorageClassKeyVO("us-west-1", "in1");
debugL("getkey1:" + getkey1 + ": val:" + storageMap.get(getkey1));
		StorageClassKeyVO getkey2 = new StorageClassKeyVO("us-east-1", "in2");
debugL("getkey2:" + getkey2 + ": val:" + storageMap.get(getkey2));
debugL("storageKeyTest: end\n");		
	}	

//********************************************************************

	@Test
    public void createJsonBucketMsg() 
		throws Exception {
debugL("=======================================");
debugL("createJsonBucketMsg:");
		BucketVO bucketVO = setupBucketVO("bucket1");
		String outStr = S3JsonResponseFormatter.createJsonBucketMsg(bucketVO);
//debugL("outStr:" + outStr + "!");
		JSONObject json = (JSONObject)(new JSONParser().parse(outStr));
debugL("createJsonBucketMsg: end");
	}

/////////////////////////////////////////////
	
	@Test
    public void createJsonBucketListMsg() 
		throws Exception {
debugL("=======================================");
debugL("createJsonBucketListMsg:");
		List<Bucket> buckets = new ArrayList<Bucket>();
		buckets.add(new Bucket("bucket1"));
		buckets.add(new Bucket("bucket2"));
		buckets.add(new Bucket("bucket3"));
		String outStr = S3JsonResponseFormatter.createJsonBucketListMsg(buckets);
//debugL("outStr:" + outStr + "!");
		JSONObject json = (JSONObject)(new JSONParser().parse(outStr));
debugL("createJsonBucketListMsg: end");
	}

/////////////////////////////////////////////
	
	@Test
    public void createJsonRegionListMsgTest() 
		throws Exception {
debugL("=======================================");
debugL("createJsonRegionListMsgTest:");
		Map<String,List<BucketVO>> regionBucketVOs = new HashMap<String,List<BucketVO>>();

		List<BucketVO> bucketVOs1 = new ArrayList<BucketVO>();
		BucketVO bucketVO11 = setupBucketVO("bucketW11");
		bucketVOs1.add(bucketVO11);
		BucketVO bucketVO12 = setupBucketVO("bucketW12");
		bucketVOs1.add(bucketVO12);
		regionBucketVOs.put("us-west-1", bucketVOs1);

		List<BucketVO> bucketVOs2 = new ArrayList<BucketVO>();
		BucketVO bucketVO21 = setupBucketVO("bucketW21");
		bucketVOs2.add(bucketVO21);
		regionBucketVOs.put("us-west-2", bucketVOs2);

//debugL("bucketVO:" + bucketVO + "!");
		String outStr = S3JsonResponseFormatter.createJsonRegionListMsg(regionBucketVOs);
//debugL("outStr:" + outStr + "!");
		JSONObject json = (JSONObject)(new JSONParser().parse(outStr));
debugL("createJsonRegionListMsgTest: end");
	}

/////////////////////////////////////////////

	@Test
    public void createJsonStorageRegionMsgTest() 
		throws Exception {
debugL("=======================================");
debugL("createJsonStorageRegionMsgTest:");
		Calendar cal = Calendar.getInstance();
		cal.set(2017, 1, 25, 12, 33, 07);
		Date date = cal.getTime();

		String region = "us-east-1";
		Map<StorageClassKeyVO,BucketVO> storageMap1 = new HashMap<StorageClassKeyVO,BucketVO>();
		BucketVO bucketVO1 = setupBucketVO("bucket1");
		StorageClassKeyVO key1 = new StorageClassKeyVO(region, bucketVO1.getBucket().getName());
		storageMap1.put(key1, bucketVO1);
		BucketVO bucketVO2 = setupBucketVO("bucket2");
		StorageClassKeyVO key2 = new StorageClassKeyVO(region, bucketVO2.getBucket().getName());
		storageMap1.put(key2, bucketVO2);

		Map<StorageClassKeyVO,BucketVO> storageMap2 = new HashMap<StorageClassKeyVO,BucketVO>();
		BucketVO bucketVO21 = setupBucketVO("bucket21");
		StorageClassKeyVO key21 = new StorageClassKeyVO(region, bucketVO21.getBucket().getName());
		storageMap2.put(key21, bucketVO21);
		BucketVO bucketVO22 = setupBucketVO("bucket22");
		StorageClassKeyVO key22 = new StorageClassKeyVO(region, bucketVO22.getBucket().getName());
		storageMap2.put(key22, bucketVO22);

		Map<String,Map<StorageClassKeyVO,BucketVO>> storageClassMap = new HashMap<String,Map<StorageClassKeyVO,BucketVO>>();
		storageClassMap.put("Standard", storageMap1);
		storageClassMap.put("RR", storageMap2);

		BucketVO bucketVO = new BucketVO();
		bucketVO.setStorageClassMap(storageClassMap);

//debugL("bucketVO:" + bucketVO + "!");
		String outStr = S3JsonResponseFormatter.createJsonStorageBucketMsg(storageClassMap);
//debugL("outStr:" + outStr + "!");
		JSONObject json = (JSONObject)(new JSONParser().parse(outStr));
		assertNotNull(json);
debugL("createJsonStorageRegionMsgTest: end");
	}
	
/////////////////////////////////////////////
	
	private BucketVO setupBucketVO( String bucketName ) {
		Calendar cal = Calendar.getInstance();
		cal.set(2017, 1, 25, 12, 33, 07);
		Date date = cal.getTime();

		Bucket bucket = new Bucket(bucketName);
		bucket.setCreationDate(date);

		BucketVO bucketVO = new BucketVO();
		bucketVO.setRegion("us-west-2");
		bucketVO.setTotalFiles(10);
		bucketVO.setTotalFilesSize(32393406392L);
		bucketVO.setLatestFileModDate(date);
		bucketVO.setLatestFilename("m1/m2.txt");
		bucketVO.setBucket(bucket);

		return bucketVO;
	}

//********************************************************

	protected void debugL( Object o ) {
		System.out.println("--S3Test: " + o);
	}
}
