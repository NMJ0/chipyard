
// See LICENSE for license details.
package chipyard.fpga.vc707

import org.chipsalliance.cde.config.{Config, Parameters}
import freechips.rocketchip.subsystem.{SystemBusKey, PeripheryBusKey, ControlBusKey, ExtMem}
import freechips.rocketchip.devices.debug.{DebugModuleKey, ExportDebug, JTAG}
import freechips.rocketchip.devices.tilelink.{DevNullParams, BootROMLocated}
import freechips.rocketchip.diplomacy.{RegionType, AddressSet}
import freechips.rocketchip.resources.{DTSModel, DTSTimebase}
import freechips.rocketchip.util.{SystemFileName}

import sifive.fpgashells.shell.{DesignKey}
import sifive.fpgashells.shell.xilinx.{VC7074GDDRSize}

import testchipip.serdes.{SerialTLKey}

import chipyard.{BuildSystem, ExtTLMem}
import chipyard.harness._

import gemmini.DefaultGemminiConfig

// ------------------------------------------------------------
// System tweaks (bootrom, memory, etc.)
// ------------------------------------------------------------
class WithSystemModifications extends Config((site, here, up) => {
  case DTSTimebase => BigInt((1e6).toLong)
  case BootROMLocated(x) => up(BootROMLocated(x), site).map { p =>
    p.copy(hang = 0x10000)
  }
  case ExtMem => up(ExtMem, site).map(x =>
    x.copy(master = x.master.copy(size = site(VC7074GDDRSize))))
  case SerialTLKey => Nil
})

// ------------------------------------------------------------
// Parameterized FPGA frequency config
// ------------------------------------------------------------
class WithFPGAFrequency(fMHz: Double) extends Config(
  new chipyard.config.WithPeripheryBusFrequency(fMHz) ++
  new chipyard.config.WithMemoryBusFrequency(fMHz) ++
  new chipyard.config.WithSystemBusFrequency(fMHz) ++
  new chipyard.config.WithControlBusFrequency(fMHz) ++
  new chipyard.config.WithFrontBusFrequency(fMHz)
)

class WithFPGAFreq20MHz  extends WithFPGAFrequency(20)
class WithFPGAFreq25MHz  extends WithFPGAFrequency(25)
class WithFPGAFreq50MHz  extends WithFPGAFrequency(50)
class WithFPGAFreq75MHz  extends WithFPGAFrequency(75)
class WithFPGAFreq100MHz extends WithFPGAFrequency(100)

// ------------------------------------------------------------
// VC707 Tweaks (NOW PARAMETERIZED)
// ------------------------------------------------------------
class WithVC707Tweaks(freqMHz: Double = 50) extends Config(

  // ---- clocking -------------------------------------------------------
  new chipyard.harness.WithAllClocksFromHarnessClockInstantiator ++
  new chipyard.clocking.WithPassthroughClockGenerator ++
  new chipyard.config.WithUniformBusFrequencies(freqMHz) ++
  new chipyard.harness.WithHarnessBinderClockFreqMHz(freqMHz) ++
  new WithFPGAFrequency(freqMHz) ++

  // ---- harness binders ------------------------------------------------
  new WithVC707UARTTSIHarnessBinder ++
  new WithVC707DDRMemHarnessBinder ++

  // ---- UART-TSI client ------------------------------------------------
  new testchipip.tsi.WithUARTTSIClient ++
  new chipyard.harness.WithSerialTLTiedOff ++

  // ---- chip-level config ----------------------------------------------
  new chipyard.config.WithNoUART ++
  new chipyard.config.WithTLBackingMemory ++
  new WithSystemModifications ++
  new chipyard.config.WithNoDebug ++
  new freechips.rocketchip.subsystem.WithoutTLMonitors ++
  new freechips.rocketchip.subsystem.WithNMemoryChannels(1)
)

// ------------------------------------------------------------
// Base configs (parameterized)
// ------------------------------------------------------------
class RocketVC707Config extends Config(
  new WithVC707Tweaks(50) ++
  new chipyard.RocketConfig
)

class RocketGemminiVC707Config extends Config(
  new DefaultGemminiConfig ++
  new WithVC707Tweaks(50) ++
  new chipyard.config.WithBroadcastManager ++
  new chipyard.RocketConfig
)

class BoomVC707Config extends Config(
  new WithVC707Tweaks(50) ++
  new chipyard.MegaBoomV3Config
)

// ------------------------------------------------------------
// NEW: 25 MHz configs
// ------------------------------------------------------------
class RocketVC70725MHzConfig extends Config(
  new WithVC707Tweaks(25) ++
  new chipyard.RocketConfig
)

class RocketGemminiVC70725MHzConfig extends Config(
  new DefaultGemminiConfig ++
  new WithVC707Tweaks(25) ++
  new chipyard.config.WithBroadcastManager ++
  new chipyard.RocketConfig
)

// ------------------------------------------------------------
// NEW: 20 MHz configs
// ------------------------------------------------------------
class RocketVC70720MHzConfig extends Config(
  new WithVC707Tweaks(20) ++
  new chipyard.RocketConfig
)

class RocketGemminiVC70720MHzConfig extends Config(
  new DefaultGemminiConfig ++
  new WithVC707Tweaks(20) ++
  new chipyard.config.WithBroadcastManager ++
  new chipyard.RocketConfig
)

