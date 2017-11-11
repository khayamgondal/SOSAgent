# @author Khayam Gondal kanjam@g.clemson.edu

$startovs = <<SCRIPT
    sudo /etc/init.d/openvswitch-switch start
SCRIPT

Vagrant.configure("2") do |config|
  config.vm.box = "geddings/mininext"

  config.vm.provider "virtualbox" do |v|
      v.customize ["modifyvm", :id, "--cpuexecutioncap", "50"]
      v.customize ["modifyvm", :id, "--memory", "2048"]
  end

  ## Guest config
  config.vm.hostname = "SOS"
  config.vm.network :private_network, type: "dhcp"


  ## Sync folders
  config.vm.synced_folder ".", "/home/vagrant/sos-agent"
  config.vm.synced_folder "../SOSForFloodlight", "/home/vagrant/sos-for-floodlight"

  ## Provisioning

  ## SSH config
  config.ssh.forward_x11 = true
  config.ssh.forward_agent = true

end