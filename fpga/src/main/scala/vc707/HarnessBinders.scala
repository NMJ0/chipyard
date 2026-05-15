package chipyard.fpga.vc707

import chisel3._
import chisel3.experimental.{BaseModule}

import org.chipsalliance.diplomacy.nodes.{HeterogeneousBag}
import freechips.rocketchip.tilelink.{TLBundle}

// UARTTSIPort is defined in chipyard.iobinders, not testchipip.serdes.
// This matches exactly how NexysVideo's HarnessBinders resolves it.
import chipyard.harness.{HarnessBinder}
import chipyard.iobinders._     // UARTTSIPort, TLMemPort, etc.
import testchipip.serdes._      // SerialTLPort etc.

/*** UART-TSI ***
 *
 * Connects the chip's UARTTSIPort to the VC707's physical UART pins via the
 * io_uart_bb bundle bridge that is declared in VC707FPGATestHarness.
 * This mirrors WithNexysVideoUARTTSI in the NexysVideo harness binders.
 */
class WithVC707UARTTSIHarnessBinder(uartBaudRate: BigInt = 115200) extends HarnessBinder({
  case (th: VC707FPGATestHarnessImp, port: UARTTSIPort, chipId: Int) => {
    // Wire the chip-side TSI UART directly to the board UART overlay.
    th.vc707Outer.io_uart_bb.bundle <> port.io.uart
    // Optionally surface the "dropped" / state signals to LEDs for debugging.
    // Uncomment the lines below if you want visibility via the VC707 LEDs:
    // th.vc707Outer.ledModule.foreach(_(0) := port.io.dropped)
  }
})

/*** DDR (unchanged) ***/
class WithVC707DDRMemHarnessBinder extends HarnessBinder({
  case (th: VC707FPGATestHarnessImp, port: TLMemPort, chipId: Int) => {
    val bundles = th.vc707Outer.ddrClient.out.map(_._1)
    val ddrClientBundle = Wire(new HeterogeneousBag(bundles.map(_.cloneType)))
    bundles.zip(ddrClientBundle).foreach { case (bundle, io) => bundle <> io }
    ddrClientBundle <> port.io
  }
})

// WithVC707SPISDCardHarnessBinder removed — SD card / sdboot flow disabled.
