package in.ac.iitm.teleop_intuitive_android;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

// [android_core/android_gingerbread_mr1]
import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;

// [rosjava_core/rosjava_bootstrap]
import org.ros.namespace.GraphName;

// [rosjava_core/rosjava]
import org.ros.address.InetAddressFactory;
import org.ros.concurrent.CancellableLoop;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;

/**
 * @author Ashish Ranjan
 *
 */
public class MainActivity extends RosActivity implements SensorEventListener {
	// android
	private Handler mHandler;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private TextView mTextView;
	private double linear_velocity;
	private double angular_velocity;
	private int flag;

	// ros
	private TalkerNode mTalkerNode;
	private volatile geometry_msgs.Twist twist;

	public MainActivity() {
		// The RosActivity constructor configures the notification title and ticker messages.
		super("Teleop Intuitive Android", "Teleop Intuitive Android");
	}

	/** Called when the activity is first created. */
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setListeners();

		mHandler = new Handler();
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
		mTextView = (TextView) findViewById(R.id.text);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		angular_velocity = event.values[2]/200;
		mTextView.setText("Angular Velocity: " + angular_velocity);
	}

	/** Called in a background thread once this {@link Activity} has been initialized with a master {@link URI} via the {@link MasterChooser} and a {@link NodeMainExecutorService} has started. */
	/** Your {@link NodeMains} should be started here using the provided {@link NodeMainExecutor}. */
	@Override
	protected void init(NodeMainExecutor nodeMainExecutor) {
		mTalkerNode = new TalkerNode();

    		NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(), getMasterUri());
		
		// Execute the supplied {@link NodeMain} using the supplied {@link NodeConfiguration}.
		nodeMainExecutor.execute(mTalkerNode, nodeConfiguration);
	}

	/**
	 * Set various listeners.
	 */
	private void setListeners() {
		// SeekBar: Change velocity
		SeekBar seekBar_velocity = (SeekBar) findViewById(R.id.seekBar1);
		seekBar_velocity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				linear_velocity = (progress/200.0);
				angular_velocity = (progress/100.0);

				if (flag == 1 || flag == 2 || flag == 3)
					twist.getLinear().setX(linear_velocity);
				if (flag == 7 || flag == 8 || flag == 9)
					twist.getLinear().setX(-linear_velocity);
				if (flag == 1 || flag == 4 || flag == 9)
					twist.getAngular().setZ(angular_velocity);
				if (flag == 3 || flag == 6 || flag == 7)
					twist.getAngular().setZ(-angular_velocity);
			}
		});

		// Button 2: FORWARD
		Button button_2 = (Button) findViewById(R.id.button2);
		button_2.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				flag = 2;
				twist.getLinear().setX(linear_velocity);
				twist.getAngular().setZ(0.0);
			}
		});

		// Button 4: TURN ANTICLOCKWISE
		Button button_4 = (Button) findViewById(R.id.button4);
		button_4.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				flag = 4;
				twist.getLinear().setX(0.0);
				twist.getAngular().setZ(angular_velocity);
			}
		});

		// Button 5: STOP
		Button button_5 = (Button) findViewById(R.id.button5);
		button_5.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				flag = 5;
				twist.getLinear().setX(0.0);
				twist.getAngular().setZ(0.0);
			}
		});

		// Button 6: TURN CLOCKWISE
		Button button_6 = (Button) findViewById(R.id.button6);
		button_6.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				flag = 6;
				twist.getLinear().setX(0.0);
				twist.getAngular().setZ(-angular_velocity);
			}
		});

		// Button 8: BACKWARD
		Button button_8 = (Button) findViewById(R.id.button8);
		button_8.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				flag = 8;
				twist.getLinear().setX(-linear_velocity);
				twist.getAngular().setZ(0.0);
			}
		});
	}

	/**
	 * @author Ashish Ranjan
	 *
	 */
	private class TalkerNode extends AbstractNodeMain {

		/** Called when ... */
		@Override
		public GraphName getDefaultNodeName() {
			return GraphName.of("teleop_intuitive_android/talker");
		}

		/** Called when the {@link Node} has started and successfully connected to the master. */
		@Override
		public void onStart(final ConnectedNode connectedNode) {
			// Display "Connected to ROS MASTER" message on the screen
			mHandler.post(new Runnable() {

				public void run() {
					Toast.makeText(MainActivity.this, "Connected to ROS MASTER", Toast.LENGTH_SHORT).show();
				}
			});

			final Publisher<geometry_msgs.Twist> publisher = connectedNode.newPublisher("cmd_vel", geometry_msgs.Twist._TYPE);
			twist = publisher.newMessage();
			twist.getLinear().setY(0.0);
			twist.getLinear().setZ(0.0);
			twist.getAngular().setX(0.0);
			twist.getAngular().setY(0.0);

			// This CancellableLoop will be canceled automatically when the node shuts down.
			connectedNode.executeCancellableLoop(new CancellableLoop() {
				private int loop_rate;

				@Override
				protected void setup() {
					loop_rate = 100;
				}

				@Override
				protected void loop() throws InterruptedException {
					twist.getAngular().setZ(angular_velocity);
					publisher.publish(twist);
					Thread.sleep(loop_rate);
				}
			});
		}
	}
}
